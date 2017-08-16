package eu.domibus.plugin.fs.worker;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Quartz based worker responsible for the periodical execution of the FSPurgeSentService.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@DisallowConcurrentExecution // Only one FSPurgeSentWorker runs at any time on the same node
public class FSPurgeSentWorker extends QuartzJobBean {

    @Autowired
    private FSPurgeSentService purgeSentService;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        purgeSentService.purgeSentFSMessages();
    }

}
