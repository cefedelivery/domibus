package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.scheduling.SchedulingTaskExecutor;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class DomainTaskExecutorImplTest {

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected SchedulingTaskExecutor taskExecutor;

    @Tested
    DomainTaskExecutorImpl domainTaskExecutor;

    @Test
    public void testSubmitRunnable(@Injectable Runnable submitRunnable) {
        domainTaskExecutor.submitRunnable(submitRunnable);

        new Verifications() {{
            taskExecutor.submit(submitRunnable);
        }};
    }

    @Test(expected = DomainException.class)
    public void testSubmitRunnableThreadInterruption(@Injectable Runnable submitRunnable,
                                                     @Injectable Future<?> utrFuture) throws Exception {
        new Expectations() {{
            taskExecutor.submit(submitRunnable);
            result = utrFuture;

            utrFuture.get(anyLong, withAny(TimeUnit.SECONDS));
            result = new InterruptedException();
        }};

        domainTaskExecutor.submitRunnable(submitRunnable);

        new Verifications() {{
            taskExecutor.submit(submitRunnable);
            times = 1;
        }};
    }
}