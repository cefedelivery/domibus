package eu.domibus.core.replication;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Catalin Enache
 * @since 4.0
 */
@DisallowConcurrentExecution
public class UIReplicationJob extends DomibusQuartzJobBean {

    /**
     * logger
     */
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationJob.class);

    @Autowired
    private UIMessageDiffService uiMessageDiffService;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        if (!uiReplicationSignalService.isReplicationEnabled()) {
            return;
        }

        LOG.debug("UIReplicationJob start");
        uiMessageDiffService.findAndSyncUIMessages();
        LOG.debug("UIReplicationJob end");
    }
}
