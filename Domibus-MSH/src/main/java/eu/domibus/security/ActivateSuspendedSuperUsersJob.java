package eu.domibus.security;

import eu.domibus.common.services.UserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.GeneralQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author Tiago Miguel
 * @since 4.0
 * <p>
 * Job in charge of unlocking suspended super user accounts.
 */
@DisallowConcurrentExecution
public class ActivateSuspendedSuperUsersJob extends GeneralQuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ActivateSuspendedSuperUsersJob.class);

    @Autowired
    private UserService userService;

    @Override
    protected void executeJob(JobExecutionContext context) {
        LOG.debug("Executing job to unlock suspended account at {}", new Date());
        userService.reactivateSuspendedUsers();
    }
}
