package eu.domibus.core.alerts.job;

import eu.domibus.core.alerts.service.AlertService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@DisallowConcurrentExecution
public class AlertCleanerSuperJob extends QuartzJobBean {

    @Autowired
    private AlertService alertService;


    @Override
    protected void executeInternal(JobExecutionContext context){
        alertService.cleanAlerts();
    }
}
