package eu.domibus.security;

import eu.domibus.common.services.UserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.GeneralQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * Job in charge of sending alerts to super-users whose passwords expired or are about to expire
 */
@DisallowConcurrentExecution
public class SuperUserPasswordPolicyAlertJob extends GeneralQuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SuperUserPasswordPolicyAlertJob.class);

    @Autowired
    private UserService userService;

    @Override
    protected void executeJob(JobExecutionContext context) {

        LOG.debug("Executing job 'check password expiration' for super-users at " + LocalDateTime.now());

        userService.triggerPasswordAlerts();
    }

}
