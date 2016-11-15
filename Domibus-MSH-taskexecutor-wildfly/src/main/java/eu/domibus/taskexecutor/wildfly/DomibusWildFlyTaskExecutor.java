package eu.domibus.taskexecutor.wildfly;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.jndi.JndiLocatorSupport;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.util.Assert;

import javax.enterprise.concurrent.ManagedExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Cosmin Baciu
 * @since 3.3
 *
 * {@link SchedulingTaskExecutor} implementation for WildFly
 */
public class DomibusWildFlyTaskExecutor extends JndiLocatorSupport implements SchedulingTaskExecutor {

    protected ManagedExecutorService executorService;

    public void execute(Runnable task) {
        Assert.state(executorService != null, "No executor service specified");

        if (task == null) {
            throw new TaskRejectedException("Executor service did not accept task because it was null");
        }

        try {
            executorService.execute(task);
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor service did not accept task: " + task, ex);
        }
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        execute(task);
    }

    @Override
    public Future<?> submit(Runnable task) {
        FutureTask<Object> future = new FutureTask<Object>(task, null);
        execute(future);
        return future;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> future = new FutureTask<T>(task);
        execute(future);
        return future;
    }

    @Override
    public boolean prefersShortLivedTasks() {
        return true;
    }

    public void setExecutorService(ManagedExecutorService executorService) {
        this.executorService = executorService;
    }
}