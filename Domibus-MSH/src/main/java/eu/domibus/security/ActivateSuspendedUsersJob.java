package eu.domibus.security;

import eu.domibus.common.services.UserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Job in charge of unlocking suspended user accounts.
 */
@DisallowConcurrentExecution
public class ActivateSuspendedUsersJob extends QuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ActivateSuspendedUsersJob.class);

    @Autowired
    private UserService userService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing job to unlock suspended account at " + new Date());
        }
        userService.findAndReactivateSuspendedUsers();
    }
}
