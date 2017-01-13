package eu.domibus.taskexecutor.weblogic;

import commonj.work.WorkException;
import commonj.work.WorkListener;
import commonj.work.WorkManager;
import commonj.work.WorkRejectedException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.commonj.DelegatingWork;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Cosmin Baciu
 */
@RunWith(JMockit.class)
public class DomibusWorkManagerTaskExecutorTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusWorkManagerTaskExecutorTest.class);

    @Tested
    DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor;

    @Test(expected = IllegalStateException.class)
    public void testExecuteWithNoWorkManager(@Injectable Runnable task) {
        domibusWorkManagerTaskExecutor.execute(task);
    }

    @Test
    public void testExecuteWithWorkManager(@Injectable Runnable task, final @Injectable WorkManager workManager) throws Exception {
        domibusWorkManagerTaskExecutor.execute(task);

        new Verifications() {{
            workManager.schedule(withAny(new DelegatingWork(new Thread())));
            times = 1;
        }};
    }

    @Test
    public void testExecuteWithWorkManagerWhenExceptionsAreThrown(@Injectable Runnable task, final @Injectable WorkManager workManager) throws Exception {
        new Expectations() {{
            workManager.schedule(withAny(new DelegatingWork(new Thread())));
            result = new WorkRejectedException();
            result = new WorkException();
        }};

        try {
            domibusWorkManagerTaskExecutor.execute(task);
            fail("It should have thrown " + TaskRejectedException.class.getCanonicalName());
        } catch (TaskRejectedException e) {
            LOG.debug(TaskRejectedException.class.getCanonicalName() + " has been catched: " + e.getMessage());
        }

        try {
            domibusWorkManagerTaskExecutor.execute(task);
            fail("It should have thrown " + SchedulingException.class.getCanonicalName());
        } catch (SchedulingException e) {
            LOG.debug(SchedulingException.class.getCanonicalName() + " has been catched: " + e.getMessage());
        }
    }

    @Test
    public void testExecuteWithWorkManagerAndWorkListener(
            @Injectable Runnable task,
            final @Injectable WorkManager workManager,
            final @Injectable WorkListener workListener) throws Exception {

        domibusWorkManagerTaskExecutor.execute(task);

        new Verifications() {{
            workManager.schedule(withAny(new DelegatingWork(new Thread())), withAny(workListener));
            times = 1;
        }};
    }

    @Test
    public void testExecuteWithStartTimeout(final @Injectable Runnable task) throws Exception {
        new Expectations(domibusWorkManagerTaskExecutor) {{
            domibusWorkManagerTaskExecutor.execute(task);
            result = null;
        }};

        domibusWorkManagerTaskExecutor.execute(task, 5);

        new Verifications() {{
            domibusWorkManagerTaskExecutor.execute(task);
            times = 1;
        }};
    }

    @Test
    public void testSubmitRunnable(final @Injectable Runnable task, final @Injectable FutureTask futureTask,
                           final @Injectable WorkManager workManager) throws Exception {
        new Expectations(domibusWorkManagerTaskExecutor) {{
            domibusWorkManagerTaskExecutor.execute(withAny(futureTask));
            result = null;
        }};

        domibusWorkManagerTaskExecutor.submit(task);

        new Verifications() {{
            domibusWorkManagerTaskExecutor.execute(withAny(futureTask));
            times = 1;
        }};
    }

    @Test
    public void testSubmitCallable(final @Injectable Runnable task, final @Injectable Callable callable,
                           final @Injectable WorkManager workManager) throws Exception {
        new Expectations(domibusWorkManagerTaskExecutor) {{
            domibusWorkManagerTaskExecutor.execute(withAny(new FutureTask<>(callable)));
            result = null;
        }};

        domibusWorkManagerTaskExecutor.submit(callable);

        new Verifications() {{
            domibusWorkManagerTaskExecutor.execute(withAny(new FutureTask<>(callable)));
            times = 1;
        }};
    }

    @Test
    public void testPrefersShortLivedTasks() throws Exception {
        assertTrue(domibusWorkManagerTaskExecutor.prefersShortLivedTasks());
    }

}
