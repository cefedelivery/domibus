package eu.domibus.common.services.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.validators.ProcessValidator;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.pull.MessagingLockService;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
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
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageExchangeServiceImpl.class);

    private static final String DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING = "domibus.receiver.certificate.validation.onsending";

    private static final String DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING = "domibus.sender.certificate.validation.onsending";

    protected static final String DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE = "domibus.pull.request.send.per.job.cycle";

    private static final String PULL = "pull";

    @Autowired
    private MessagingDao messagingDao;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("pullMessageQueue")
    private Queue pullMessageQueue;

    @Autowired
    private JmsTemplate jmsPullTemplate;

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    private ProcessValidator processValidator;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private PolicyService policyService;

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    private JMSManager jmsManager;

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
    @Transactional(readOnly = true)
    public MessageStatus retrieveMessageRestoreStatus(final String messageId) {
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        try {
            MessageExchangeConfiguration userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            return getMessageStatus(userMessageExchangeConfiguration);
        } catch (EbMS3Exception e) {
            throw new PModeException(DomibusCoreErrorCode.DOM_001, "Could not get the PMode key for message [" + messageId + "]", e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void initiatePullRequest() {
        if (!pModeProvider.isConfigurationLoaded()) {
            LOG.debug("A configuration problem occurred while initiating the pull request. Probably no configuration is loaded.");
            return;
        }
        Party initiator = pModeProvider.getGatewayParty();
        List<Process> pullProcesses = pModeProvider.findPullProcessesByInitiator(initiator);
        LOG.trace("Initiating pull requests:");
        final Integer numberOfPullRequestPerMpc = Integer.valueOf(domibusProperties.getProperty(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE, "1"));
        if (pause(pullProcesses, numberOfPullRequestPerMpc)) {
            return;
        }
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
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(messageExchangeConfiguration.toString());
                        }
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

                        final Integer numberOfPullRequestPerMpc = Integer.valueOf(domibusProperties.getProperty(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE, "1"));
                        LOG.debug("Sending:[{}] pull request for mpc:[{}]", numberOfPullRequestPerMpc, mpcQualifiedName);
                        for (int i = 0; i < numberOfPullRequestPerMpc; i++) {
                            jmsPullTemplate.convertAndSend(pullMessageQueue, map, postProcessor);
                        }

                    }
                }
            } catch (PModeException e) {
                LOG.warn("Invalid pull process configuration found during pull try " + e.getMessage());
            }
        }

    }

    private boolean pause(List<Process> pullProcesses, int numberOfPullRequestPerMpc) {
        LOG.trace("Checking if the system should pause the pulling mechanism.");
        int numberOfPullMpc = 0;
        final long queueMessageNumber = jmsManager.getDestinationSize(PULL);
        for (Process pullProcess : pullProcesses) {
            try {
                processValidator.validatePullProcess(Lists.newArrayList(pullProcess));
                numberOfPullMpc++;
            } catch (PModeException e) {
                LOG.warn("Invalid pull process configuration found during pull try " + e.getMessage());
            }
        }

        final int pullRequestsToSendCount = numberOfPullMpc * numberOfPullRequestPerMpc;
        final boolean shouldPause = queueMessageNumber > pullRequestsToSendCount;
        if (shouldPause) {
            LOG.debug("[PULL]:Size of the pulling queue:[{}] is higher then the number of pull requests to send:[{}]. Pause adding to the queue so the system can consume the requests.", queueMessageNumber, pullRequestsToSendCount);
        } else {
            LOG.trace("[PULL]:Size of the pulling queue:[{}], the number of pull requests to send:[{}].", queueMessageNumber, pullRequestsToSendCount);
        }
        return shouldPause;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String retrieveReadyToPullUserMessageId(final String mpc, final Party initiator) {
        Set<Identifier> identifiers = initiator.getIdentifiers();
        if (identifiers.size() == 0) {
            LOG.warn("No identifier found for party:[{}]", initiator.getName());
            return null;
        }
        String partyId = identifiers.iterator().next().getPartyId();
        return pullMessageService.getPullMessageId(partyId, mpc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PullContext extractProcessOnMpc(final String mpcQualifiedName) {
        try {
            final Party gatewayParty = pModeProvider.getGatewayParty();
            List<Process> processes = pModeProvider.findPullProcessByMpc(mpcQualifiedName);
            if (LOG.isDebugEnabled()) {
                for (Process process : processes) {
                    LOG.debug("Process:[{}] correspond to mpc:[{}]", process.getName(), mpcQualifiedName);
                }
            }
            processValidator.validatePullProcess(processes);
            return new PullContext(processes.get(0), gatewayParty, mpcQualifiedName);
        } catch (IllegalArgumentException e) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, "No pmode configuration found");
        }
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

    /**
     * This method is a bit weird as we delete and save a xml message for the same message id.
     * Saving the raw xml message in the case of the pull is occuring on the last outgoing interceptor in order
     * to have all the cxf message modification saved (reliability check.) Unfortunately this saving is not done in the
     * same transaction.
     *
     * @param rawXml    the soap envelope
     * @param messageId the user message
     */
    @Override
    @Transactional
    public void saveRawXml(String rawXml, String messageId) {
        RawEnvelopeLog newRawEnvelopeLog = new RawEnvelopeLog();
        newRawEnvelopeLog.setRawXML(rawXml);
        newRawEnvelopeLog.setMessageId(messageId);
        rawEnvelopeLogDao.create(newRawEnvelopeLog);
    }

    @Override
    @Transactional(noRollbackFor = ChainCertificateInvalidException.class)
    public void verifyReceiverCertificate(final LegConfiguration legConfiguration, String receiverName) {
        Policy policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
        if (policyService.isNoSecurityPolicy(policy)) {
            return;
        }
        if(Boolean.parseBoolean(domibusPropertyProvider.getProperty(DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING, "true"))) {
            String chainExceptionMessage = "Cannot send message: receiver certificate is not valid or it has been revoked [" + receiverName + "]";
            try {
                boolean certificateChainValid = multiDomainCertificateProvider.isCertificateChainValid(domainProvider.getCurrentDomain(), receiverName);
                if (!certificateChainValid) {
                    throw new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, chainExceptionMessage);
                }
                LOG.info("Receiver certificate exists and is valid [" + receiverName + "]");
            } catch (DomibusCertificateException e) {
                throw new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, chainExceptionMessage, e);
            }
        }
    }

    @Override
    @Transactional(noRollbackFor = ChainCertificateInvalidException.class)
    public void verifySenderCertificate(final LegConfiguration legConfiguration, String senderName) {
        Policy policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
        if (policyService.isNoSecurityPolicy(policy)) {
            return;
        }
        if(Boolean.parseBoolean(domibusPropertyProvider.getProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING, "true"))) {
            String chainExceptionMessage = "Cannot send message: sender certificate is not valid or it has been revoked [" + senderName + "]";
            try {
                X509Certificate certificate = multiDomainCertificateProvider.getCertificateFromKeystore(domainProvider.getCurrentDomain(), senderName);
                if (certificate == null) {
                    throw new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, "Cannot send message: sender[" + senderName + "] certificate not found in Keystore");
                }
                if (!certificateService.isCertificateValid(certificate)) {
                    throw new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, chainExceptionMessage);
                }
                LOG.info("Sender certificate exists and is valid [" + senderName + "]");
            } catch (DomibusCertificateException | KeyStoreException ex) {
                // Is this an error and we stop the sending or we just log a warning that we were not able to validate the cert?
                // my opinion is that since the option is enabled, we should validate no matter what => this is an error
                throw new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, chainExceptionMessage, ex);
            }
        }
    }
}

