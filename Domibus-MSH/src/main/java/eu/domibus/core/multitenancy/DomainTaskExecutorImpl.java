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

    @Override
    public <T extends Object> T submit(Callable<T> task) {
        DomainCallable domainCallable = new DomainCallable(domainContextProvider, task);
        final Future<T> utrFuture = schedulingTaskExecutor.submit(domainCallable);
        try {
            return utrFuture.get(5000L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new DomainException("Could not execute task", e);
        }
    }

    @Override
    public void submit(Runnable task) {
        LOG.trace("Submitting task");
        final ClearDomainRunnable clearDomainRunnable = new ClearDomainRunnable(domainContextProvider, task);
        submitRunnable(clearDomainRunnable, true, DEFAULT_WAIT_TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    public void submit(Runnable task, Domain domain) {
        submit(task, domain, true, DEFAULT_WAIT_TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    public void submit(Runnable task, Domain domain, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        LOG.trace("Submitting task for domain [{}]", domain);
        final DomainRunnable domainRunnable = new DomainRunnable(domainContextProvider, domain, task);
        submitRunnable(domainRunnable, waitForTask, timeout, timeUnit);
    }

    protected void submitRunnable(Runnable task, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        final Future<?> utrFuture = schedulingTaskExecutor.submit(task);

        if (waitForTask) {
            LOG.debug("Waiting for task to complete");
            try {
                utrFuture.get(timeout, timeUnit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DomainException("Could not execute task", e);
            } catch (ExecutionException | TimeoutException e) {
                throw new DomainException("Could not execute task", e);
            }
        }
    }
}
