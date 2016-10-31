package eu.domibus.taskexecutor.weblogic;

import commonj.work.*;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.jndi.JndiLocatorSupport;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author Cosmin Baciu
 * @since 3.3
 *
 * {@link SchedulingTaskExecutor} implementation for WebLogic
 */
public class DomibusWorkManagerTaskExecutor extends JndiLocatorSupport implements SchedulingTaskExecutor {

    protected WorkManager workManager;

    protected WorkListener workListener;

    @Override
    public void execute(Runnable task) {
        Assert.state(workManager != null, "No WorkManager specified");
        if (task == null) {
            throw new TaskRejectedException("CommonJ WorkManager did not accept task because it was null");
        }

        Work work = new DomibusDelegatingWork(task);
        try {
            if (workListener != null) {
                workManager.schedule(work, workListener);
            } else {
                workManager.schedule(work);
            }
        } catch (WorkRejectedException ex) {
            throw new TaskRejectedException("CommonJ WorkManager did not accept task: " + task, ex);
        } catch (WorkException ex) {
            throw new SchedulingException("Could not schedule task on CommonJ WorkManager", ex);
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

    public boolean prefersShortLivedTasks() {
        return true;
    }

    public void setWorkManager(WorkManager workManager) {
        this.workManager = workManager;
    }

    public void setWorkListener(WorkListener workListener) {
        this.workListener = workListener;
    }

}