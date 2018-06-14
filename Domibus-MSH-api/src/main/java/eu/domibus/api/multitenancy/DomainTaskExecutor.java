package eu.domibus.api.multitenancy;

import java.util.concurrent.Callable;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainTaskExecutor {

    <T extends Object> T submit(Callable<T> task);
}
