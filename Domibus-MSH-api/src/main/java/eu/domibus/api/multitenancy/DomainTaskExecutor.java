package eu.domibus.api.multitenancy;

import java.util.concurrent.Callable;

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

    void submitLongRunningTask(Runnable task, Runnable errorHandler, Domain domain);

    void submitLongRunningTask(Runnable task, Domain domain);
}
