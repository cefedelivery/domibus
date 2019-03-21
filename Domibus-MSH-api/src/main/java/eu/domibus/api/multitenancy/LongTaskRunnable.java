package eu.domibus.api.multitenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the Runnable class to be executed. Catches any exception and logs it.
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public class LongTaskRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LongTaskRunnable.class);

    protected Runnable runnable;
    protected Runnable errorHandler;

    public LongTaskRunnable(final Runnable runnable, Runnable errorHandler) {
        this.runnable = runnable;
        this.errorHandler = errorHandler;
    }

    public LongTaskRunnable(final Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        try {
            LOG.trace("Start executing task");
            runnable.run();
            LOG.trace("Finished executing task");
        } catch (Throwable e) {
            LOG.error("Error executing task", e);

            executeErrorHandler();
        }
    }

    protected void executeErrorHandler() {
        if (errorHandler == null) {
            LOG.trace("No error handler has been set");
            return;
        }

        try {
            LOG.trace("Start executing error handler");
            errorHandler.run();
            LOG.trace("Finished executing error handler");
        } catch (Throwable e) {
            LOG.error("Error executing error handler", e);
        }
    }
}
