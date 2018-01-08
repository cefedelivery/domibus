package eu.domibus.quartz;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JUnit for {@link DomibusQuartzStarter}
 *
 * @author Catalin Enache
 * @version 1.0
 * @since 08/01/2018
 */
@RunWith(JMockit.class)
public class DomibusQuartzStarterTest {

    private final String groupName = "DEFAULT";
    private final List<String> jobGroups = Collections.singletonList(groupName);
    private final Set<JobKey> jobKeys =  new HashSet<>();
    private final JobKey jobKey1 = new JobKey("retryWorkerJob", groupName);

    @Tested
    private DomibusQuartzStarter domibusQuartzStarter;

    @Injectable
    private Scheduler scheduler;

    @Before
    public void setUp() throws Exception {
        jobKeys.add(jobKey1);

        //disable post-construct method
        new MockUp<DomibusQuartzStarter>() {
            @Mock
            public void checkJobsAndStartScheduler() throws SchedulerException {
                //do nothing
            }
        };
    }

    @Test
    public void checkSchedulerJobs_ValidConfig_NoJobDeleted(final @Mocked JobDetailImpl jobDetail) throws Exception {

        new Expectations() {{
            scheduler.getJobGroupNames();
            times = 1;
            result = jobGroups;

            scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
            times = 1;
            result = jobKeys;

            scheduler.getJobDetail(jobKey1);
            times = 1;
            result = jobDetail;

            jobDetail.getJobClass();
            times = 1;
            result = Class.forName("eu.domibus.ebms3.sender.SendRetryWorker");

        }};

        //tested method
        domibusQuartzStarter.checkSchedulerJobs();

        new FullVerifications() {{
        }};
    }

    @Test
    public void checkSchedulerJobs_InvalidConfig_JobDeleted(final @Mocked JobDetailImpl jobDetail) throws Exception {



        new Expectations() {{
            scheduler.getJobGroupNames();
            times = 1;
            result = jobGroups;

            scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
            times = 1;
            result = jobKeys;

            scheduler.getJobDetail(jobKey1);
            times = 1;
            result = jobDetail;

            jobDetail.getJobClass();
            times = 1;
            result = new SchedulerException(new ClassNotFoundException("required class was not found: eu.domibus.ebms3.sender.SendRetryWorker"));

        }};

        //tested method
        domibusQuartzStarter.checkSchedulerJobs();

        new FullVerifications() {{
            JobKey jobKeyActual;
            scheduler.deleteJob(jobKeyActual = withCapture());
            times = 1;
            Assert.assertEquals(jobKey1, jobKeyActual);
        }};
    }

}