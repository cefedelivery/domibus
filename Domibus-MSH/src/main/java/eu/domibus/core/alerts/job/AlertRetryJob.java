package eu.domibus.core.alerts.job;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class AlertRetryJob extends DomibusQuartzJobBean {

    private final static Logger LOG = LoggerFactory.getLogger(AlertRetryJob.class);

    @Autowired
    private AlertService alertService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {
        alertService.retrieveAndResendFailedAlerts();
    }
}
