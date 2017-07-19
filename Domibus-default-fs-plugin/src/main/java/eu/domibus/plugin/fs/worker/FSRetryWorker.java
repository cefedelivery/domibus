package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Quartz based worker responsible for the periodical execution of the FSRetryService.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@DisallowConcurrentExecution // Only one FSRetryWorker runs at any time on the same node
public class FSRetryWorker extends QuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSRetryWorker.class);

    @Autowired
    private FSRetryService retryService;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        retryService.resendFailedFSMessages();
    }

}
