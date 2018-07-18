package eu.domibus.core.replication;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

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

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        uiReplicationDataService.findAndSyncUIMessages();
    }
}
