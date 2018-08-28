package eu.domibus.core.replication;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
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
    private UIReplicationDataService uiReplicationDataService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        boolean uiReplicationEnabled = Boolean.parseBoolean(domibusPropertyProvider.getProperty("domibus.ui.replication.enabled", "false"));

        if (!uiReplicationEnabled) {
            LOG.debug("UIReplication is disabled - no processing will occur");
            return;
        }

        LOG.debug("UIReplicationJob start");
        uiReplicationDataService.findAndSyncUIMessages();
    }
}
