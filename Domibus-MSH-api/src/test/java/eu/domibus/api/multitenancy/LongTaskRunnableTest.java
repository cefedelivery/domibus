package eu.domibus.api.multitenancy;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class LongTaskRunnableTest {

    @Injectable
    protected Runnable runnable;

    @Injectable
    protected Runnable errorHandler;

    @Test
    public void run() {
        new Expectations() {{
            runnable.run();
            result = new DomainTaskException("long running task exception");
        }};

        LongTaskRunnable longTaskRunnable = new LongTaskRunnable(runnable, errorHandler);
        longTaskRunnable.run();

        new Verifications() {{
            errorHandler.run();
        }};
    }
}