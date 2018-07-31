package eu.domibus.ebms3.sender;


import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.quartz.DomibusQuartzJobBean;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.core.pull.PullMessageStateService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SendRetryWorker.class);

    @Autowired
    protected RetryService retryService;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    private PullMessageStateService pullMessageStateService;

    @Autowired
    protected AuthUtils authUtils;


    @Override
    protected void executeJob(final JobExecutionContext context, final Domain domain) throws JobExecutionException {
        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password");
        }

        try {
            retryService.enqueueMessages();
        } catch (Exception e) {
            LOG.error("Error while eqnueing messages.", e);
        }
        try {
            retryService.bulkExpirePullMessages();
        } catch (Exception e) {
            LOG.error("Error while bulk expiring pull messages.", e);
        }

        try {
            retryService.resetWaitingForReceiptPullMessages();
        } catch (Exception e) {
            LOG.error("Error while reseting waiting for receipt.", e);
        }

        try {
            retryService.bulkDeletePullMessages();
        } catch (Exception e) {
            LOG.error("Error while bulk deleting messages.", e);
        }


    }
}
