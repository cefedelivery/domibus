package eu.domibus.quartz;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

import javax.annotation.PostConstruct;

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
public class DomibusQuartzStarter {

    /** logger */
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusQuartzStarter.class);

    /** injected scheduler by SchedulerFactoryBean */
    private Scheduler scheduler;

    /**
     * entry point method (post-construct)
     *
     * @throws SchedulerException Quartz scheduler exception
     */
    @PostConstruct
    public void checkJobsAndStartScheduler() throws SchedulerException {

        //check Quartz scheduler jobs first
        checkSchedulerJobs();

        scheduler.start();
        LOG.info("Quartz scheduler started.");
    }


    /** scheduler's setter */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    /**
     * run through scheduler jobs and check for ClassNotFoundException
     *
     * @throws SchedulerException Qurtz scheduler exception
     */
    void checkSchedulerJobs() throws SchedulerException {
        LOG.info("Start checking Quartz jobs...");

        //go through jobs to see which one throws ClassNotFoundException
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                final String jobName = jobKey.getName();
                final String jobGroup = jobKey.getGroup();

                LOG.debug("Found Quartz job: " + jobName + " from group: " + jobGroup);

                try {
                    scheduler.getJobDetail(jobKey).getJobClass().getName();
                } catch (SchedulerException se) {
                    if (ExceptionUtils.getRootCause(se) instanceof ClassNotFoundException) {
                        try {
                            scheduler.deleteJob(jobKey);
                            LOG.warn("DELETED Quartz job: " + jobName + " from group: " + jobGroup + " cause: " + se.getMessage());
                        } catch (Exception e) {
                            LOG.error("Error while deleting Quartz job: " + jobName, e);
                        }
                    }
                }
            }
        }
    }


}
