package eu.domibus.quartz;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public abstract class DomibusQuartzJobBean extends QuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusQuartzJobBean.class);


    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            LOG.clearCustomKeys();
            final Domain currentDomain = getDomain(context);
            domainContextProvider.setCurrentDomain(currentDomain);
            executeJob(context, currentDomain);
        } finally {
            domainContextProvider.clearCurrentDomain();
            LOG.clearCustomKeys();
        }
    }

    protected Domain getDomain(JobExecutionContext context) throws JobExecutionException {
        try {
            final String schedulerName = context.getScheduler().getSchedulerName();
            return domainService.getDomainForScheduler(schedulerName);
        } catch (SchedulerException e) {
            throw new JobExecutionException("Could not get Quartz Scheduler", e);
        }
    }

    protected abstract void executeJob(final JobExecutionContext context, final Domain domain) throws JobExecutionException;

}
