package eu.domibus.api.multitenancy;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Task executor used to schedule tasks that are issuing queries against the general schema from an already started transaction.
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainTaskExecutor {

    <T extends Object> T submit(Callable<T> task);

    void submit(Runnable task);

    void submit(Runnable task, Domain domain);

    void submit(Runnable task, Domain domain, boolean waitForTask, Long timeout, TimeUnit timeUnit);
}
