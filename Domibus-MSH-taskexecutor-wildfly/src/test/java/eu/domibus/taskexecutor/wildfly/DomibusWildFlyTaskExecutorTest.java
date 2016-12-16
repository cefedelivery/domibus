package eu.domibus.taskexecutor.wildfly;

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

import javax.enterprise.concurrent.ManagedExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Cosmin Baciu
 */
@RunWith(JMockit.class)
public class DomibusWildFlyTaskExecutorTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusWildFlyTaskExecutorTest.class);

    @Tested
    DomibusWildFlyTaskExecutor domibusWildFlyTaskExecutor;

    @Test(expected = IllegalStateException.class)
    public void testExecuteWithNoExecutorService(@Injectable Runnable task) {
        domibusWildFlyTaskExecutor.execute(task);
    }

    @Test
    public void testExecuteWithWorkManager(final @Injectable Runnable task, final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        domibusWildFlyTaskExecutor.execute(task);

        new Verifications() {{
            managedExecutorService.execute(task);
            times = 1;
        }};
    }

    @Test
    public void testExecuteWithWorkManagerWhenExceptionsAreThrown(final @Injectable Runnable task, final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        new Expectations() {{
            managedExecutorService.execute(task);
            result = new RejectedExecutionException();
        }};

        try {
            domibusWildFlyTaskExecutor.execute(task);
            fail("It should have thrown " + TaskRejectedException.class.getCanonicalName());
        } catch (TaskRejectedException e) {
            LOG.debug(TaskRejectedException.class.getCanonicalName() + " has been catched: " + e.getMessage());
        }
    }

    @Test
    public void testExecuteWithStartTimeout(final @Injectable Runnable task) throws Exception {
        new Expectations(domibusWildFlyTaskExecutor) {{
            domibusWildFlyTaskExecutor.execute(task);
            result = null;
        }};

        domibusWildFlyTaskExecutor.execute(task, 5);

        new Verifications() {{
            domibusWildFlyTaskExecutor.execute(task);
            times = 1;
        }};
    }


    @Test
    public void testSubmitRunnable(final @Injectable Runnable task, final @Injectable FutureTask futureTask,
                                   final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        new Expectations(domibusWildFlyTaskExecutor) {{
            domibusWildFlyTaskExecutor.execute(withAny(futureTask));
            result = null;
        }};

        domibusWildFlyTaskExecutor.submit(task);

        new Verifications() {{
            domibusWildFlyTaskExecutor.execute(withAny(futureTask));
            times = 1;
        }};
    }

    @Test
    public void testSubmitCallable(final @Injectable Runnable task, final @Injectable Callable callable,
                                   final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        new Expectations(domibusWildFlyTaskExecutor) {{
            domibusWildFlyTaskExecutor.execute(withAny(new FutureTask<>(callable)));
            result = null;
        }};

        domibusWildFlyTaskExecutor.submit(callable);

        new Verifications() {{
            domibusWildFlyTaskExecutor.execute(withAny(new FutureTask<>(callable)));
            times = 1;
        }};
    }

    @Test
    public void testPrefersShortLivedTasks() throws Exception {
        assertTrue(domibusWildFlyTaskExecutor.prefersShortLivedTasks());
    }

}
