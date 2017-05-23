package eu.domibus.ebms3.puller;

import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.dao.ProcessDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeContext;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.xml.soap.SOAPMessage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dussath on 5/23/17.
 * <p>
 * This class will check recurently (based on a cron configuration) in the  PMODE config to find potential pull configurations.
 * If pull configurations are found, it will create a pullrequest.
 */
@DisallowConcurrentExecution //Only one SenderWorker runs at any time
//@dussath test this class
public class MessagePuller extends QuartzJobBean {
    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagePuller.class);
    @Autowired
    private ProcessDao processDao;
    @Autowired
    private ConfigurationDAO configurationDAO;
    @Autowired
    private MSHDispatcher mshDispatcher;
    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("Check for pull PMODE");
        PullContext pullContext = extractConfigurationInfo();
        List<Process> pullProcesses = processDao.findPullProcessesByIniator(pullContext.currentMsh);
        LOG.info(pullProcesses.size()+" pull PMODE found!");
        for (Process pullProcess : pullProcesses) {
            pullContext.process = pullProcess;
            checkProcessValidity(pullContext);
            if (!pullContext.valid) {
                continue;
            }
            for (Party responderParty : pullContext.getResponderParties()) {
                pullContext.toBePulled = responderParty;
                instantiateSoapMessage(pullContext);
                if (!pullContext.valid) {
                    continue;
                }
                try {
                    final SOAPMessage response = mshDispatcher.dispatch(pullContext.pullMessage, pullContext.pModeKey);
                } catch (EbMS3Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }

        }
    }

    private void instantiateSoapMessage(PullContext pullContext) {
        MessageExchangeContext messageExchangeContext = new MessageExchangeContext(pullContext.getAgreement(), pullContext.getCurrentMSHName(), pullContext.getToBePulledName(), pullContext.getServiceName(), pullContext.getActionName(), pullContext.getLegName());
        pullContext.pModeKey = messageExchangeContext.getPmodeKey();
        PullRequest pullRequest = new PullRequest();
        pullRequest.setMpc(pullContext.getMpcName());
        SignalMessage signalMessage = new SignalMessage();
        signalMessage.setPullRequest(pullRequest);
        try {
            pullContext.pullMessage = messageBuilder.buildSOAPMessage(signalMessage, pullContext.getLegConfiguration());
        } catch (EbMS3Exception e) {
            LOG.error(e.getMessage(), e);
            pullContext.valid = false;
        }
    }

    private PullContext extractConfigurationInfo() {
        PullContext pullContext = new PullContext();
        Configuration configuration = configurationDAO.read();
        pullContext.currentMsh = configuration.getParty();
        return pullContext;
    }


    private void checkProcessValidity(PullContext pullContext) {
        if (pullContext.process.getLegs().size() > 1) {
            LOG.error("Only one leg authorized in a oneway pull. PMode skipped!");
            pullContext.valid = false;
        }
        if (pullContext.process.getLegs().size() == 0) {
            LOG.error("No legs configured. PMode skipped!");
            pullContext.valid = false;
        }
    }


    private class PullContext {
        Process process;
        Party currentMsh;
        Party toBePulled;
        boolean valid = true;
        SOAPMessage pullMessage;
        String pModeKey;

        public Set<Party> getResponderParties() {
            Set<Party> responderParties = new HashSet<>(process.getResponderParties());
            responderParties.remove(currentMsh);
            return responderParties;
        }

        public String getAgreement() {
            if (process.getAgreement() != null) {
                return process.getAgreement().getName();
            }
            return "";
        }

        String getCurrentMSHName() {
            return currentMsh.getName();
        }

        String getToBePulledName() {
            return toBePulled.getName();
        }

        String getLegName() {
            return process.getLegs().iterator().next().getName();
        }

        String getActionName() {
            return process.getLegs().iterator().next().getAction().getName();
        }

        String getServiceName() {
            return process.getLegs().iterator().next().getService().getName();
        }

        String getMpcName() {
            Mpc defaultMpc = process.getLegs().iterator().next().getDefaultMpc();
            if (defaultMpc != null) {
                return defaultMpc.getName();
            }
            return "";
        }

        LegConfiguration getLegConfiguration() {
            return process.getLegs().iterator().next();
        }
    }
    static class PullBuilder{

    }
}
