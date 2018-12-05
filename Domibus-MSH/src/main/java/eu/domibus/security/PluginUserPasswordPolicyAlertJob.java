package eu.domibus.security;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.common.services.PluginUserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * Job in charge of sending alerts about plugin users whose passwords expired or are about to expire
 */
@DisallowConcurrentExecution
public class PluginUserPasswordPolicyAlertJob extends DomibusQuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginUserPasswordPolicyAlertJob.class);

    @Autowired
    private PluginUserService pluginUserService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {

        LOG.debug("Executing job 'check password expiration' for users at " + LocalDateTime.now());

        pluginUserService.triggerPasswordAlerts();
    }

}
