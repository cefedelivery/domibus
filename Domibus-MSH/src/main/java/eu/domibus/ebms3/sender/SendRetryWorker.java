package eu.domibus.ebms3.sender;


import eu.domibus.ebms3.security.util.AuthUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Quartz based worker responsible for the periodical execution of {@link eu.domibus.ebms3.sender.MessageSender#sendUserMessage(String)}
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class SendRetryWorker extends QuartzJobBean {

    @Autowired
    private RetryService retryService;

    @Autowired
    AuthUtils authUtils;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {

        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password");
        }

        retryService.enqueueMessages();
    }


}
