package eu.domibus.ebms3.sender;


import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.core.pull.PullMessageStateService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SendRetryWorker.class);

    @Autowired
    private RetryService retryService;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    private PullMessageStateService pullMessageStateService;

    @Autowired
    AuthUtils authUtils;


    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {

        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password");
        }

        try {
            retryService.enqueueMessages();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        try {
            pullMessageService.resetWaitingForReceiptPullMessages();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        try {
            pullMessageStateService.bulkExpirePullMessages();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        try {
            retryService.purgePullMessage();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }


}
