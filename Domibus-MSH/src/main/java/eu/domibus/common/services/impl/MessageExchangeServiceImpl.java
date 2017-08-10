package eu.domibus.common.services.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.validators.ProcessValidator;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.MessagePullDto;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.DomibusCertificateException;
import eu.domibus.pki.PolicyService;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static eu.domibus.common.MessageStatus.READY_TO_PULL;
import static eu.domibus.common.MessageStatus.SEND_ENQUEUED;
import static eu.domibus.common.services.impl.PullContext.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 * {@inheritDoc}
 */
@Service
public class MessageExchangeServiceImpl implements MessageExchangeService {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageExchangeService.class);

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    @Qualifier("pullMessageQueue")
    private Queue pullMessageQueue;

    @Autowired
    private JmsTemplate jmsPullTemplate;

    @Autowired
    private UserMessageLogService userMessageLogService;

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    private ProcessValidator processValidator;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    @Qualifier("domibusProperties")
    private java.util.Properties domibusProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MessageStatus getMessageStatus(final MessageExchangeConfiguration messageExchangeConfiguration) {
        MessageStatus messageStatus = SEND_ENQUEUED;
        List<Process> processes = pModeProvider.findPullProcessesByMessageContext(messageExchangeConfiguration);
        if (!processes.isEmpty()) {
            processValidator.validatePullProcess(Lists.newArrayList(processes));
            messageStatus = READY_TO_PULL;
        } else {
            LOG.debug("No pull process found for message configuration");
        }
        return messageStatus;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void initiatePullRequest() {
        if (!configurationDAO.configurationExists()) {
            return;
        }
        Configuration configuration = configurationDAO.read();
        Party initiator = configuration.getParty();
        List<Process> pullProcesses = pModeProvider.findPullProcessesByInitiator(initiator);
        for (Process pullProcess : pullProcesses) {
            try {
                processValidator.validatePullProcess(Lists.newArrayList(pullProcess));
                for (LegConfiguration legConfiguration : pullProcess.getLegs()) {
                    for (Party initiatorParty : pullProcess.getResponderParties()) {
                        String mpcQualifiedName = legConfiguration.getDefaultMpc().getQualifiedName();
                        //@thom remove the pullcontext from here.
                        PullContext pullContext = new PullContext(pullProcess,
                                initiatorParty,
                                mpcQualifiedName);
                        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration(pullContext.getAgreement(),
                                initiatorParty.getName(),
                                initiator.getName(),
                                legConfiguration.getService().getName(),
                                legConfiguration.getAction().getName(),
                                legConfiguration.getName());

                        final Map<String, String> map = Maps.newHashMap();
                        map.put(MPC, mpcQualifiedName);
                        map.put(PMODE_KEY, messageExchangeConfiguration.getReversePmodeKey());
                        map.put(PullContext.NOTIFY_BUSINNES_ON_ERROR, String.valueOf(legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()));
                        MessagePostProcessor postProcessor = new MessagePostProcessor() {
                            public Message postProcessMessage(Message message) throws JMSException {
                                message.setStringProperty(MPC, map.get(MPC));
                                message.setStringProperty(PMODE_KEY, map.get(PMODE_KEY));
                                message.setStringProperty(NOTIFY_BUSINNES_ON_ERROR, map.get(NOTIFY_BUSINNES_ON_ERROR));
                                return message;
                            }
                        };
                        jmsPullTemplate.convertAndSend(pullMessageQueue, map, postProcessor);

                    }
                }
            } catch (PModeException e) {
                LOG.warn("Invalid pull process configuration found during pull try " + e.getMessage());
            }
        }

    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String retrieveReadyToPullUserMessageId(final String mpc, final Party initiator) {
        Set<Identifier> identifiers = initiator.getIdentifiers();
        List<MessagePullDto> messagingOnStatusReceiverAndMpc = new ArrayList<>();
        for (Identifier identifier : identifiers) {
            messagingOnStatusReceiverAndMpc.addAll(messagingDao.findMessagingOnStatusReceiverAndMpc(identifier.getPartyId(), MessageStatus.READY_TO_PULL, mpc));
        }

        if (!messagingOnStatusReceiverAndMpc.isEmpty()) {
            MessagePullDto messagePullDto = messagingOnStatusReceiverAndMpc.get(0);
            userMessageLogService.setIntermediaryPullStatus(messagePullDto.getMessageId());
            return messagePullDto.getMessageId();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PullContext extractProcessOnMpc(final String mpcQualifiedName) {
        if (!configurationDAO.configurationExists()) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, "No pmode configuration found");
        }
        List<Process> processes = pModeProvider.findPullProcessByMpc(mpcQualifiedName);
        Configuration configuration = configurationDAO.read();
        processValidator.validatePullProcess(processes);
        return new PullContext(processes.get(0), configuration.getParty(), mpcQualifiedName);
    }


    @Override
    @Transactional(noRollbackFor = ReliabilityException.class)
    public void removeRawMessageIssuedByPullRequest(final String messageId) {
        RawEnvelopeDto rawEnvelopeDto = findPulledMessageRawXmlByMessageId(messageId);
        if (rawEnvelopeDto != null) {
            rawEnvelopeLogDao.deleteRawMessage(rawEnvelopeDto.getId());
        }
    }

    @Override
    @Transactional
    public void savePulledMessageRawXml(final String rawXml, final String messageId) {
        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        RawEnvelopeLog rawEnvelopeLog = new RawEnvelopeLog();
        rawEnvelopeLog.setRawXML(rawXml);
        rawEnvelopeLog.setUserMessage(userMessage);
        rawEnvelopeLogDao.create(rawEnvelopeLog);
    }

    @Override
    @Transactional(noRollbackFor = ReliabilityException.class)
    public RawEnvelopeDto findPulledMessageRawXmlByMessageId(final String messageId) {
        final RawEnvelopeDto rawXmlByMessageId = rawEnvelopeLogDao.findRawXmlByMessageId(messageId);
        if (rawXmlByMessageId == null) {
            throw new ReliabilityException(DomibusCoreErrorCode.DOM_004, "There should always have a raw message for message " + messageId);
        }
        return rawXmlByMessageId;
    }


    @Override
    public void verifyReceiverCerficate(final LegConfiguration legConfiguration, String receiverName) {
        Policy policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
        if (policyService.isNoSecurityPolicy(policy)) {
            return;
        }
        final String domibusReceiverCertificateValidationOnsending = "domibus.receiver.certificate.validation.onsending";
        if (Boolean.parseBoolean(domibusProperties.getProperty(domibusReceiverCertificateValidationOnsending, "true"))) {
            final ChainCertificateInvalidException chainCertificateInvalidException = new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, "Cannot send message: receiver certificate is not valid or it has been revoked [" + receiverName + "]");
            try {
                boolean certificateChainValid = certificateService.isCertificateChainValid(receiverName);
                if (!certificateChainValid) {
                    throw chainCertificateInvalidException;
                }
                LOG.info("Receiver certificate exists and is valid [" + receiverName + "]");
            } catch (DomibusCertificateException e) {
                throw chainCertificateInvalidException;
            }
        }
    }

    @Override
    public void verifySenderCertificate(final LegConfiguration legConfiguration, String senderName) {
        Policy policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
        if (policyService.isNoSecurityPolicy(policy)) {
            return;
        }
        final String domibusSenderCertificateValidationOnSending = "domibus.sender.certificate.validation.onsending";
        if (Boolean.parseBoolean(domibusProperties.getProperty(domibusSenderCertificateValidationOnSending, "true"))) {
            final ChainCertificateInvalidException chainCertificateInvalidException = new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, "Cannot send message: sender certificate is not valid or it has been revoked [" + senderName + "]");
            try {
                if (!certificateService.isCertificateChainValid(senderName)) {
                    throw chainCertificateInvalidException;
                }
                LOG.info("Sender certificate exists and is valid [" + senderName + "]");
            } catch (DomibusCertificateException dce) {
                // Is this an error and we stop the sending or we just log a warning that we were not able to validate the cert?
                // my opinion is that since the option is enabled, we should validate no matter what => this is an error
                throw chainCertificateInvalidException;
            }
        }
    }
}

