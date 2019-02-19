package eu.domibus.security;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.common.services.UserService;
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
 * Job in charge of sending alerts to users whose passwords expired or are about to expire
 */
@DisallowConcurrentExecution
public class UserPasswordPolicyAlertJob extends DomibusQuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserPasswordPolicyAlertJob.class);

    @Autowired
    private UserService userService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {

        LOG.debug("Executing job 'check password expiration' for users at " + LocalDateTime.now());

        userService.triggerPasswordAlerts();
    }

}
