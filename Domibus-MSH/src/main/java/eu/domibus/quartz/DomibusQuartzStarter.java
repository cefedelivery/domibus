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
 * @author Catalin Enache
 * @version 1.0
 * @since 05/01/2018
 */
public class DomibusQuartzStarter {

    /** logger */
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusQuartzStarter.class);

    /** injected scheduler by SchedulerBeanFactory */
    private Scheduler scheduler;

    @PostConstruct
    public void checkJobsAndStartScheduler() throws SchedulerException {

        LOG.info("Quartz scheduler -> start checking jobs...");

        //go through jobs to see which one throws ClassCastException
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                LOG.info("Found jobName=" + jobName + " from groupName=" + jobGroup);

                try {
                    scheduler.getJobDetail(jobKey).getJobClass().getName();
                } catch (SchedulerException se) {
                    if (ExceptionUtils.getRootCause(se) instanceof ClassNotFoundException) {
                        try {
                            scheduler.deleteJob(jobKey);
                            LOG.warn("Quartz scheduler -> DELETED jobName=" + jobName + " from groupName=" + jobGroup + " cause: " + se.getMessage());
                        } catch (Exception e) {
                            LOG.error("Error while deleting job: " + jobName, e);
                        }
                    }
                }
            }
        }

        scheduler.start();
        LOG.info("Quartz scheduler started...");

    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
