package eu.domibus.common.services.impl;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.*;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import eu.domibus.common.services.MessageExchangeService;
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
import static eu.domibus.common.services.impl.PullRequestStatus.ONE_MATCHING_PROCESS;

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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public void upgradeMessageExchangeStatus(MessageExchangeConfiguration messageExchangeConfiguration) {
        List<Process> processes = processDao.findProcessByMessageContext(messageExchangeConfiguration);
        messageExchangeConfiguration.updateStatus(MessageStatus.SEND_ENQUEUED);
        for (Process process : processes) {
            boolean pullProcess = BackendConnector.Mode.PULL.getFileMapping().equals(Process.getBindingValue(process));
            boolean oneWay = BackendConnector.Mep.ONE_WAY.getFileMapping().equals(Process.getMepValue(process));
            if (pullProcess) {
                if (!oneWay) {
                    throw new RuntimeException("We only support oneway/pull at the moment");
                }
                if (processes.size() > 1) {
                    throw new RuntimeException("This configuration is also mapping another process!");
                }
                PullContext pullContext = new PullContext();
                pullContext.setProcess(process);
                pullContext.checkProcessValidity();
                if (!pullContext.isValid()) {
                    throw new RuntimeException(pullContext.createProcessWarningMessage());
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
        LOG.info("Check for pull PMODE");
        Configuration configuration = configurationDAO.read();
        List<Process> pullProcesses = processDao.findPullProcessesByResponder(configuration.getParty());
        LOG.info(pullProcesses.size() + " pull PMODE found!");
        for (Process pullProcess : pullProcesses) {
            PullContext pullContext = new PullContext();
            pullContext.setResponder(configuration.getParty());
            pullContext.setProcess(pullProcess);
            pullContext.checkProcessValidity();
            if (!pullContext.isValid()) {
                continue;
            }
            pullContext.send(new PullContextCommand() {
                @Override
                public void execute(final Map<String, String> messageMap) {
                    MessagePostProcessor postProcessor = new MessagePostProcessor() {
                        public Message postProcessMessage(Message message) throws JMSException {
                            message.setStringProperty(MPC, messageMap.get("mpc"));
                            message.setStringProperty(PMODE_KEY, messageMap.get("mpc"));
                            return message;
                        }
                    };
                    jmsPullTemplate.convertAndSend(pullMessageQueue, messageMap, postProcessor);
                }
            });
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
        PullContext pullContext = new PullContext();
        pullContext.addRequestStatus(ONE_MATCHING_PROCESS);
        pullContext.setMpcQualifiedName(mpcQualifiedName);
        findCurrentAccesPoint(pullContext);
        finMpcProcess(pullContext);
        pullContext.setResponder(pullContext.getProcess().getResponderParties().iterator().next());
        pullContext.checkProcessValidity();
        return pullContext;
    }

    /**
     * Retrieve process information based on the information contained in the pullRequest.
     *
     * @param pullContext the context of the request.
     */
    //@thom test this method
    private void finMpcProcess(PullContext pullContext) {
        List<Process> processes = processDao.findPullProcessBytMpc(pullContext.getMpcQualifiedName());
        if (processes.size() > 1) {
            pullContext.addRequestStatus(PullRequestStatus.TOO_MANY_PROCESSES);
        } else if (processes.size() == 0) {
            pullContext.addRequestStatus(PullRequestStatus.NO_PROCESSES);
        } else {
            pullContext.setProcess(processes.get(0));
        }
    }

    /**
     * Extract initiator and responder information based on the pullrequest.
     *
     * @param pullContext the context of the pull request.
     */
    //@thom test this method
    private void findCurrentAccesPoint(PullContext pullContext) {
        Configuration configuration = configurationDAO.read();
        pullContext.setInitiator(configuration.getParty());
    }

    @Override
    @Transactional
    public void savePulledMessageRawXml(final String rawXml, final String messageId) {
        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId.toString());
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

