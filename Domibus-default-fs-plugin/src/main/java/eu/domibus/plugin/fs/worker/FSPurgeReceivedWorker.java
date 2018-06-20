package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.quartz.DomibusQuartzJobExtBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Quartz based worker responsible for the periodical execution of the FSPurgeReceivedService.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@DisallowConcurrentExecution // Only one FSPurgeReceivedWorker runs at any time on the same node
public class FSPurgeReceivedWorker extends DomibusQuartzJobExtBean {

    @Autowired
    private FSPurgeReceivedService purgeReceivedService;

    @Override
    protected void executeJob(JobExecutionContext context, DomainDTO domain) {
        purgeReceivedService.purgeMessages();
    }

}
