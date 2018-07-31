package eu.domibus.quartz;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quartz scheduler starter class which:
 * <p>
 * 1. checks existing jobs - if {@code ClassNotFoundException} is thrown - it deletes the job.
 * It could be the case of FS-PLUGIN which leaves metadata in {@code QRTZ_*} tables
 * <p>
 * 2. starts manually the Quartz scheduler
 *
 * @author Catalin Enache
 * @version 1.0
 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean
 * @since 3.3.2
 */
@Service
public class DomibusQuartzStarter {

    /**
     * logger
     */
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusQuartzStarter.class);

    @Autowired
    protected DomibusSchedulerFactory domibusSchedulerFactory;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    protected Map<Domain, Scheduler> schedulers = new HashMap<>();

    protected List<Scheduler> generalSchedulers = new ArrayList<>();

    @PostConstruct
    public void initQuartzSchedulers() {
        // General Schedulers
        try {
            startsSchedulers("general");
        } catch (SchedulerException e) {
            LOG.error("Could not initialize the Quartz Scheduler for general schema", e);
        }

        // Domain Schedulers
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            try {
                checkJobsAndStartScheduler(domain);
            } catch (SchedulerException e) {
                LOG.error("Could not initialize the Quartz Scheduler for domain [{}]", e, domain);
            }
        }
    }

    @PreDestroy
    public void shutdownQuartzSchedulers() {
        LOG.debug("Shutting down Quartz Schedulers");

        // General Schedulers
        for(Scheduler scheduler : generalSchedulers) {
            try {
                scheduler.shutdown(true);
            } catch (SchedulerException e) {
                LOG.error("Error while shutting down Quartz Scheduler for general schema", e);
            }
        }

        // Domain Schedulers
        for (Map.Entry<Domain, Scheduler> domainSchedulerEntry : schedulers.entrySet()) {
            final Domain domain = domainSchedulerEntry.getKey();
            LOG.debug("Shutting down Quartz Scheduler for domain [{}]", domain);
            final Scheduler quartzScheduler = domainSchedulerEntry.getValue();
            try {
                quartzScheduler.shutdown(true);
            } catch (SchedulerException e) {
                LOG.error("Error while shutting down Quartz Scheduler for domain [{}]", e, domain);
            }
        }
    }

    /**
     * entry point method (post-construct)
     *
     * @throws SchedulerException Quartz scheduler exception
     */
    public void checkJobsAndStartScheduler(Domain domain) throws SchedulerException {
        Scheduler scheduler = domibusSchedulerFactory.createScheduler(domain);

        //check Quartz scheduler jobs first
        checkSchedulerJobs(scheduler);

        scheduler.start();
        schedulers.put(domain, scheduler);
        LOG.info("Quartz scheduler started for domain [{}]", domain);
    }

    /**
     * Starts scheduler with trigger group equals to {@code triggerGroup}, only if in multi tenant scenario
     *
     * @throws SchedulerException Quartz scheduler exception
     */
    private void startsSchedulers(String triggerGroup) throws SchedulerException {
        if (!domibusConfigurationService.isMultiTenantAware()) {
            return;
        }
        Scheduler generalScheduler = domibusSchedulerFactory.createScheduler(null);

        //check Quartz scheduler jobs first
        checkSchedulerJobsByTriggerGroup(generalScheduler, triggerGroup);

        generalScheduler.start();
        generalSchedulers.add(generalScheduler);
        LOG.info("Quartz scheduler started for general schema");
    }

    /**
     * Checks for all the jobs related with trigger group {@code triggerGroup}
     * @param scheduler Scheduler
     * @param triggerGroup Trigger Group name
     */
    protected void checkSchedulerJobsByTriggerGroup(Scheduler scheduler, String triggerGroup) throws SchedulerException {
        LOG.info("Start Quartz jobs with trigger group [{}]...", triggerGroup);

        for(TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroup))) {
            Trigger trigger = scheduler.getTrigger(triggerKey);
            JobKey jobKey = trigger.getJobKey();

            try {
                scheduler.getJobDetail(jobKey).getJobClass().getName();
            } catch (SchedulerException se) {
                if (ExceptionUtils.getRootCause(se) instanceof ClassNotFoundException) {
                    try {
                        scheduler.deleteJob(jobKey);
                        LOG.warn("DELETED Quartz job: {} from group: {} cause: {}", jobKey.getName(), jobKey.getGroup(), se.getMessage());
                    } catch (Exception e) {
                        LOG.error("Error while deleting Quartz job: {}", jobKey.getName(), e);
                    }
                }
            }
        }
    }

    /**
     * goes through scheduler jobs and check for {@code ClassNotFoundException}
     *
     * @throws SchedulerException Quartz scheduler exception
     */
    protected void checkSchedulerJobs(Scheduler scheduler) throws SchedulerException {
        LOG.info("Start checking Quartz jobs...");

        for (String groupName : scheduler.getJobGroupNames()) {
            checkSchedulerJobsFromGroup(scheduler, groupName);
        }
    }

    /**
     * check scheduler jobs from a given group
     *
     * @param groupName scheduler group name
     * @throws SchedulerException scheduler exception
     */
    private void checkSchedulerJobsFromGroup(Scheduler scheduler, final String groupName) throws SchedulerException {

        //go through jobs to see which one throws ClassNotFoundException
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

            final String jobName = jobKey.getName();
            final String jobGroup = jobKey.getGroup();

            LOG.info("Found Quartz job: {} from group: {}", jobName, jobGroup);

            try {
                scheduler.getJobDetail(jobKey).getJobClass().getName();
            } catch (SchedulerException se) {
                if (ExceptionUtils.getRootCause(se) instanceof ClassNotFoundException) {
                    try {
                        scheduler.deleteJob(jobKey);
                        LOG.warn("DELETED Quartz job: {} from group: {} cause: {}", jobName, jobGroup, se.getMessage());
                    } catch (Exception e) {
                        LOG.error("Error while deleting Quartz job: {}", jobName, e);
                    }
                }
            }
        }
    }


}
