package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.*;
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
        final DomainRunnable domainRunnable = new DomainRunnable(domainContextProvider, task);
        final Future<?> utrFuture = schedulingTaskExecutor.submit(domainRunnable);
        try {
            utrFuture.get(5000L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new DomainException("Could not execute task", e);
        }
    }
}
