package eu.domibus.ebms3.puller;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class MessagePullerJob extends DomibusQuartzJobBean {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagePullerJob.class);

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        try {
            messageExchangeService.initiatePullRequest();
        } catch (PModeException e) {
            LOG.warn("Invalid pmode configuration for pull request " + e.getMessage());
        }
    }
}
