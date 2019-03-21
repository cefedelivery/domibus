package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainTaskExecutorImpl implements DomainTaskExecutor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainTaskExecutorImpl.class);
    public static final long DEFAULT_WAIT_TIMEOUT = 5000L;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Qualifier("taskExecutor")
    @Autowired
    protected SchedulingTaskExecutor schedulingTaskExecutor;

    @Qualifier("quartzTaskExecutor")
    @Autowired
    protected SchedulingTaskExecutor schedulingLongTaskExecutor;

    @Override
    public <T extends Object> T submit(Callable<T> task) {
        DomainCallable domainCallable = new DomainCallable(domainContextProvider, task);
        final Future<T> utrFuture = schedulingTaskExecutor.submit(domainCallable);
        try {
            return utrFuture.get(5000L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new DomainTaskException("Could not execute task", e);
        }
    }

    @Override
    public void submit(Runnable task) {
        LOG.trace("Submitting task");
        final ClearDomainRunnable clearDomainRunnable = new ClearDomainRunnable(domainContextProvider, task);
        submitRunnable(schedulingTaskExecutor, clearDomainRunnable, true, DEFAULT_WAIT_TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    public void submit(Runnable task, Domain domain) {
        submit(schedulingTaskExecutor, task, domain, true, DEFAULT_WAIT_TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    public void submitLongRunningTask(Runnable task, Domain domain) {
        submitLongRunningTask(task, null, domain);
    }

    @Override
    public void submitLongRunningTask(Runnable task, Runnable errorHandler, Domain domain) {
        submit(schedulingLongTaskExecutor, new LongTaskRunnable(task, errorHandler), domain, false, null, null);
    }

    protected void submit(SchedulingTaskExecutor taskExecutor, Runnable task, Domain domain, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        LOG.trace("Submitting task for domain [{}]", domain);
        final DomainRunnable domainRunnable = new DomainRunnable(domainContextProvider, domain, task);
        submitRunnable(taskExecutor, domainRunnable, waitForTask, timeout, timeUnit);
    }

    protected void submitRunnable(SchedulingTaskExecutor taskExecutor, Runnable task, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        final Future<?> utrFuture = taskExecutor.submit(task);

        if (waitForTask) {
            LOG.debug("Waiting for task to complete");
            try {
                utrFuture.get(timeout, timeUnit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DomainTaskException("Could not execute task", e);
            } catch (ExecutionException | TimeoutException e) {
                throw new DomainTaskException("Could not execute task", e);
            }
        }
    }
}
