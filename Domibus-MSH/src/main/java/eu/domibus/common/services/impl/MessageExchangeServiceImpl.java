package eu.domibus.common.services.impl;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.ebms3.common.context.MessageExchangeContext;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import java.util.List;

import static eu.domibus.common.MessageStatus.READY_TO_PULL;
import static eu.domibus.common.services.impl.PullRequestStatus.*;

/**
 * Created by dussath on 5/19/17.
 * {@inheritDoc}
 */
@Service
public class MessageExchangeServiceImpl implements MessageExchangeService {

    @Autowired
    private ProcessDao processDao;
    @Autowired
    private ConfigurationDAO configurationDAO;
    @Autowired
    private UserMessageLogDao userMessageLogDao;
    @Autowired
    private MessagingDao messagingDao;
    @Autowired
    private MSHDispatcher mshDispatcher;
    @Autowired
    private EbMS3MessageBuilder messageBuilder;
    @Autowired
    private PartyDao partyDao;

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagePullerServiceImpl.class);
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public void upgradeMessageExchangeStatus(MessageExchangeContext messageExchangeContext) {
        List<Process> processes = processDao.findProcessForMessageContext(messageExchangeContext);
        messageExchangeContext.updateStatus(MessageStatus.SEND_ENQUEUED);
        for (Process process : processes) {
            boolean pullProcess = BackendConnector.Mode.PULL.getFileMapping().equals(Process.getBindingValue(process));
            boolean oneWay = BackendConnector.Mep.ONE_WAY.getFileMapping().equals(Process.getMepValue(process));
            //@question wich exception should be throwned here.
            if (pullProcess) {
                if (!oneWay) {
                    throw new RuntimeException("We only support oneway/pull at the moment");
                }
                if (processes.size() > 1) {
                    throw new RuntimeException("This configuration is also mapping another process!");
                }
                messageExchangeContext.updateStatus(READY_TO_PULL);
            }
        }
    }


    @Override
    @Transactional
    /**
     * {@inheritDoc}
     */
    public void initiatePullRequest() {
        LOG.info("Check for pull PMODE");
        PullContext pullContext = extractConfigurationInfo();
        List<Process> pullProcesses = processDao.findPullProcessesByIniator(pullContext.getCurrentMsh());
        LOG.info(pullProcesses.size() + " pull PMODE found!");
        for (Process pullProcess : pullProcesses) {
            pullContext.setProcess(pullProcess);
            checkProcessValidity(pullContext);
            if (!pullContext.isValid()) {
                continue;
            }
            for (Party responderParty : pullContext.getResponderParties()) {
                pullContext.setToBePulled(responderParty);
                instantiateSoapMessage(pullContext);
                if (!pullContext.isValid()) {
                    continue;
                }
                try {
                    //@question should we use a queue here? I yes should we use the existing one, meaning every pullrequest will be stored in db?
                    final SOAPMessage response = mshDispatcher.dispatch(pullContext.getPullMessage(), pullContext.getpModeKey());
                } catch (EbMS3Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    void instantiateSoapMessage(PullContext pullContext) {
        MessageExchangeContext messageExchangeContext = new MessageExchangeContext(pullContext.getAgreement(), pullContext.getCurrentMSHName(), pullContext.getToBePulledName(), pullContext.getServiceName(), pullContext.getActionName(), pullContext.getLegName());
        pullContext.setpModeKey(messageExchangeContext.getPmodeKey());
        PullRequest pullRequest = new PullRequest();
        pullRequest.setMpc(pullContext.getMpcName());
        SignalMessage signalMessage = new SignalMessage();
        signalMessage.setPullRequest(pullRequest);
        try {
            pullContext.setPullMessage(messageBuilder.buildSOAPMessage(signalMessage, pullContext.getLegConfiguration()));
        } catch (EbMS3Exception e) {
            LOG.error(e.getMessage(), e);
            pullContext.addRequestStatus(INVALID_SOAP_MESSAGE);
        }
    }

    PullContext extractConfigurationInfo() {
        PullContext pullContext = new PullContext();
        Configuration configuration = configurationDAO.read();
        pullContext.setCurrentMsh(configuration.getParty());
        return pullContext;
    }


    void checkProcessValidity(PullContext pullContext) {
        if (pullContext.getProcess().getLegs().size() > 1) {
            LOG.error("Only one leg authorized in a oneway pull. PMode skipped!");
            pullContext.addRequestStatus(TOO_MANY_PROCESS_LEGS);
        }
        if (pullContext.getProcess().getLegs().size() == 0) {
            LOG.error("No legs configured. PMode skipped!");
            pullContext.addRequestStatus(NO_PROCESS_LEG);
        }
    }


    public UserMessage retrieveUserMessage(final SignalMessage signalMessage) {
        List<String> readyToPullMessages = userMessageLogDao.findReadyToPullMessages();
        if (!readyToPullMessages.isEmpty()) {
            String messageId = readyToPullMessages.get(0);
            /**
             * @question should we already change the status of the message here (new transaction), in case it is a big file, another request might come
             * in and extract the same
             * Do I need to do something specific to load the payload.
             */

            return messagingDao.findUserMessageByMessageId(messageId);
        }
        return null;
    }

    public PullContext extractPullRequestProcessInformation(final String initiatorName, final String mpcQualifiedName){
        PullContext pullContext = new PullContext();
        pullContext.addRequestStatus(ONE_MATCHING_PROCESS);
        pullContext.setInitiatorName(initiatorName);
        pullContext.setMpcName(mpcQualifiedName);
        configureParties(pullContext);
        configureProcess(pullContext);
        checkProcessValidity(pullContext);
        MessageExchangeContext messageExchangeContext = new MessageExchangeContext(pullContext.getAgreement(), pullContext.getCurrentMSHName(), pullContext.getToBePulledName(), pullContext.getServiceName(), pullContext.getActionName(), pullContext.getLegName());
        pullContext.setpModeKey(messageExchangeContext.getPmodeKey());
        return pullContext;
    }

    /**
     * Retrieve process information based on the information contained in the pullRequest.
     * @param pullContext the context of the request.
     */
     void configureProcess(PullContext pullContext) {
        List<Process> processes = processDao.findPullProcessFromRequestPartyAndMpc(pullContext.getInitiatorName(), pullContext.getMpcName());
        Process process=null;
        if(processes.size()>1){
            pullContext.addRequestStatus(PullRequestStatus.TOO_MANY_PROCESSES);
        }
        else if(processes.size()==0){
            pullContext.addRequestStatus(PullRequestStatus.NO_PROCESSES);
        }
        else{
            pullContext.setProcess(process);
        }
    }

    /**
     * Extract initiator and responder information based on the pullrequest.
     * @param pullContext
     */
    void configureParties(PullContext pullContext) {
        Configuration configuration = configurationDAO.read();
        pullContext.setCurrentMsh(configuration.getParty());
        Party initiator = partyDao.findPartyByName(pullContext.getInitiatorName());
        if(initiator==null){
            pullContext.addRequestStatus(INITIATOR_NOT_FOUND);
            return;
        }
        pullContext.setToBePulled(initiator);
    }


}

