package eu.domibus.quartz;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Thomas Dussart
 * @since 4.0.1
 */
public abstract class GeneralQuartzJobBean extends QuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(GeneralQuartzJobBean.class);


    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            LOG.clearCustomKeys();
            domainContextProvider.clearCurrentDomain();
            executeJob(context);
        } finally {
            domainContextProvider.clearCurrentDomain();
            LOG.clearCustomKeys();
        }
    }

    protected abstract void executeJob(final JobExecutionContext context) throws JobExecutionException;

}
