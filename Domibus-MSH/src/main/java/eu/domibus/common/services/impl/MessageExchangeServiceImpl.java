package eu.domibus.common.services.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
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
import eu.domibus.plugin.BackendConnector;
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
import static eu.domibus.common.services.impl.PullContext.MPC;
import static eu.domibus.common.services.impl.PullContext.PMODE_KEY;

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
    public void upgradeMessageExchangeStatus(MessageExchangeConfiguration messageExchangeConfiguration) throws EbMS3Exception {
        List<Process> processes = processDao.findProcessByMessageContext(messageExchangeConfiguration);
        messageExchangeConfiguration.updateStatus(MessageStatus.SEND_ENQUEUED);
        for (Process process : processes) {

            boolean pullProcess = BackendConnector.Mode.PULL.getFileMapping().equals(Process.getBindingValue(process));
            boolean oneWay = BackendConnector.Mep.ONE_WAY.getFileMapping().equals(Process.getMepValue(process));
            if (pullProcess) {
                processValidator.validatePullProcess(Lists.newArrayList(process));
                //@thom add those checks in the validator.
                if (!oneWay) {
                    throw new RuntimeException("We only support oneway/pull at the moment");
                }
                if (processes.size() > 1) {
                    throw new RuntimeException("This configuration is also mapping another process!");
                }
                messageExchangeConfiguration.updateStatus(READY_TO_PULL);
            }
        }
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
                                message.setStringProperty(MPC, map.get("mpc"));
                                message.setStringProperty(PMODE_KEY, map.get("mpc"));
                                return message;
                            }
                        };
                        jmsPullTemplate.convertAndSend(pullMessageQueue, map, postProcessor);

                    }
                }
            } catch (EbMS3Exception e) {
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
            return new PullContext("Pmode not configured");
        }
        List<Process> processes = processDao.findPullProcessBytMpc(mpcQualifiedName);
        Configuration configuration = configurationDAO.read();
        try {
            processValidator.validatePullProcess(processes);
            return new PullContext(processes.get(0), configuration.getParty(), mpcQualifiedName);
        } catch (EbMS3Exception e) {
            return new PullContext(e.getMessage());
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

