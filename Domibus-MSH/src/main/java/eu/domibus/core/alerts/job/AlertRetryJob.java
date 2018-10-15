package eu.domibus.core.alerts.job;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@DisallowConcurrentExecution
public class AlertRetryJob extends DomibusQuartzJobBean {

    @Autowired
    private AlertService alertService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {
        alertService.retrieveAndResendFailedAlerts();
    }
}
