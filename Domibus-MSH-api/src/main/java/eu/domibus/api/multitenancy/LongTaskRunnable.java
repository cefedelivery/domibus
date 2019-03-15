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


    public LongTaskRunnable(final Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Throwable e) {
            LOG.error("Error executing task", e);
        }

    }
}
