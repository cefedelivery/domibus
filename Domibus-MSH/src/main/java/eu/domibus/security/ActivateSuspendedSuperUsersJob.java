package eu.domibus.security;

import eu.domibus.common.services.UserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

/**
 * @author Tiago Miguel
 * @since 4.0
 * <p>
 * Job in charge of unlocking suspended super user accounts.
 */
@DisallowConcurrentExecution
public class ActivateSuspendedSuperUsersJob extends QuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ActivateSuspendedSuperUsersJob.class);

    @Autowired
    private UserService userService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        LOG.debug("Executing job to unlock suspended account at {}", new Date());
        userService.reactivateSuspendedUsers();
    }
}
