package eu.domibus.common.services.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.*;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.validators.ProcessValidator;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.MessagePullDto;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Service;
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
    //@thom add more coverage here.
    @Autowired
    private ProcessDao processDao;
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
    private UserMessageLogDao messageLogDao;


    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    private ProcessValidator processValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MessageStatus getMessageStatus(final MessageExchangeConfiguration messageExchangeConfiguration) {
        MessageStatus messageStatus = SEND_ENQUEUED;
        List<Process> processes = processDao.findPullProcessesByMessageContext(messageExchangeConfiguration);
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
        Party responderParty = configuration.getParty();
        List<Process> pullProcesses = processDao.findPullProcessesByResponder(responderParty);
        for (Process pullProcess : pullProcesses) {
            try {
                processValidator.validatePullProcess(Lists.newArrayList(pullProcess));
                for (LegConfiguration legConfiguration : pullProcess.getLegs()) {
                    for (Party initiatorParty : pullProcess.getInitiatorParties()) {
                        String mpcQualifiedName = legConfiguration.getDefaultMpc().getQualifiedName();
                        //@thom remove the pullcontext from here.
                        PullContext pullContext = new PullContext(pullProcess,
                                initiatorParty,
                                mpcQualifiedName);
                        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration(pullContext.getAgreement(),
                                initiatorParty.getName(),
                                responderParty.getName(),
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
    @Transactional
    public UserMessage retrieveReadyToPullUserMessages(final String mpc, final Party responder) {
        Set<Identifier> identifiers = responder.getIdentifiers();
        List<MessagePullDto> messagingOnStatusReceiverAndMpc = new ArrayList<>();
        for (Identifier identifier : identifiers) {
            messagingOnStatusReceiverAndMpc.addAll(messagingDao.findMessagingOnStatusReceiverAndMpc(identifier.getPartyId(), MessageStatus.READY_TO_PULL, mpc));
        }

        if (!messagingOnStatusReceiverAndMpc.isEmpty()) {
            MessagePullDto messagePullDto = messagingOnStatusReceiverAndMpc.get(0);
            UserMessage userMessageByMessageId = messagingDao.findUserMessageByMessageId(messagePullDto.getMessageId());
            messageLogDao.setIntermediaryPullStatus(messagePullDto.getMessageId());
            return userMessageByMessageId;
        }
        return null;


    }

    /**
     * {@inheritDoc}
     *
     * @thom test this method
     */
    @Override
    public PullContext extractProcessOnMpc(final String mpcQualifiedName) {
        if (!configurationDAO.configurationExists()) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, "No pmode configuration found");
        }
        List<Process> processes = processDao.findPullProcessBytMpc(mpcQualifiedName);
        Configuration configuration = configurationDAO.read();
        processValidator.validatePullProcess(processes);
        return new PullContext(processes.get(0), configuration.getParty(), mpcQualifiedName);
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

    @Transactional
    @Override
    public void removeRawMessageIssuedByPullRequest(final String messageId) {
        RawEnvelopeDto rawEnvelopeDto = findPulledMessageRawXmlByMessageId(messageId);
        if (rawEnvelopeDto != null) {
            rawEnvelopeLogDao.deleteRawMessage(rawEnvelopeDto.getId());
        }
    }

    @Override
    @Transactional
    public RawEnvelopeDto findPulledMessageRawXmlByMessageId(final String messageId) {
        List<RawEnvelopeDto> rawEnvelopeDto = rawEnvelopeLogDao.findRawXmlByMessageId(messageId);
        if (rawEnvelopeDto.size() == 0 || rawEnvelopeDto.size() > 1) {
            LOG.error("There should always have a raw message in the case of a pulledMessage");
            return null;
        }
        return rawEnvelopeDto.get(0);
    }


}

