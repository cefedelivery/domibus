package eu.domibus.core.alerts.job;

import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.quartz.GeneralQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 4.0.1
 */
@DisallowConcurrentExecution
public class AlertCleanerSuperJob extends GeneralQuartzJobBean {

    @Autowired
    private AlertService alertService;

    @Override
    protected void executeJob(JobExecutionContext context) {
        alertService.cleanAlerts();
    }
}
