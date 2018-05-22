package eu.domibus.ebms3.sender;


import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Quartz based worker responsible for the periodical execution of {@link eu.domibus.ebms3.sender.MessageSender#sendUserMessage(String)}
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class SendRetryWorker extends DomibusQuartzJobBean {

    @Autowired
    protected RetryService retryService;

    @Autowired
    protected AuthUtils authUtils;

    @Override
    protected void executeJob(final JobExecutionContext context, final Domain domain) throws JobExecutionException {
        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password");
        }

        retryService.enqueueMessages();
    }
}
