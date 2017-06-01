package eu.domibus.common.services.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.ebms3.common.context.MessageExchangeContext;
import eu.domibus.ebms3.common.model.MessagePullDto;
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
import java.util.Collection;
import java.util.Iterator;
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
    private MessagingDao messagingDao;
    @Autowired
    private MSHDispatcher mshDispatcher;
    @Autowired
    private EbMS3MessageBuilder messageBuilder;


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
            pullContext.checkProcessValidity();
            if (!pullContext.isValid()) {
                continue;
            }
            for (Party initiator : pullContext.getInitiatorParties()) {
                pullContext.setInitiator(initiator);
                Iterator<LegConfiguration> iterator = pullProcess.getLegs().iterator();
                while (iterator.hasNext()){
                    pullContext.setCurrentLegConfiguration(iterator.next());
                    instantiateSoapMessage(pullContext);
                }
                if (!pullContext.isValid()) {
                    continue;
                }
                try {
                    //@question should we use a queue here? I yes should we use the existing one, meaning every pullrequest will be stored in db?
                    //@question should we use multiple queues? create one per mpc to tackle fast and slow messaging?
                    final SOAPMessage response = mshDispatcher.dispatch(pullContext.getPullMessage(), pullContext.getpModeKey());
                } catch (EbMS3Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    void instantiateSoapMessage(PullContext pullContext) {
        MessageExchangeContext messageExchangeContext = new MessageExchangeContext(pullContext.getAgreement(), pullContext.getCurrentMSHName(), pullContext.getResponderName(), pullContext.getServiceName(), pullContext.getActionName(), pullContext.getLegName());
        pullContext.setpModeKey(messageExchangeContext.getPmodeKey());
        PullRequest pullRequest = new PullRequest();
        pullRequest.setMpc(pullContext.extractQualifiedNameFromProcess());
        SignalMessage signalMessage = new SignalMessage();
        signalMessage.setPullRequest(pullRequest);
        //@thom the soap message should not be instaciated here.
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
        pullContext.addRequestStatus(ONE_MATCHING_PROCESS);
        return pullContext;
    }




    @Override
    public UserMessage retrieveUserReadyToPullMessages(final String mpc, final Party responder) {
        List<MessagePullDto> messagingOnStatusReceiverAndMpc = messagingDao.findMessagingOnStatusReceiverAndMpc(responder.getEntityId(), MessageStatus.READY_TO_PULL, mpc);
        if(!messagingOnStatusReceiverAndMpc.isEmpty()){
            MessagePullDto messagePullDto = messagingOnStatusReceiverAndMpc.get(0);
            return messagingDao.findUserMessageByMessageId(messagePullDto.getMessageId());
            //@thom change the status of the message in a new transaction. Set it back to ready_to_pull after a time.
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
        pullContext.checkProcessValidity();
        pullContext.setResponder(pullContext.getProcess().getResponderParties().iterator().next());
        return pullContext;
    }

    /**
     * Retrieve process information based on the information contained in the pullRequest.
     *
     * @param pullContext the context of the request.
     * @thom test this method
     */
    void finMpcProcess(PullContext pullContext) {
        List<Process> processes = processDao.findPullProcessFromRequestMpc(pullContext.getMpcQualifiedName());
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
     * @param pullContext
     * @thom test this method
     */
    void findCurrentAccesPoint(PullContext pullContext) {
        Configuration configuration = configurationDAO.read();
        pullContext.setCurrentMsh(configuration.getParty());
    }


}

