package eu.domibus.plugin.fs.worker;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Quartz based worker responsible for the periodical execution of the FSPurgeFailedService.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@DisallowConcurrentExecution // Only one FSPurgeFailedWorker runs at any time on the same node
public class FSPurgeFailedWorker extends QuartzJobBean {

    @Autowired
    private FSPurgeFailedService purgeFailedService;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        purgeFailedService.purgeMessages();
    }

}
