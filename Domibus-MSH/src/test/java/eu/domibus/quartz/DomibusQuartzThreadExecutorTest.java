package eu.domibus.quartz;

import eu.domibus.spring.SpringContextProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.FutureTask;

/**
 * @author baciu
 */
@RunWith(JMockit.class)
public class DomibusQuartzThreadExecutorTest {

    @Tested
    DomibusQuartzThreadExecutor domibusQuartzThreadExecutor;

    @Test
    public void testExecute(final @Injectable Thread thread, final @Injectable TaskExecutor taskExecutor, @Mocked SpringContextProvider springContextProvider) throws Exception {
        new Expectations() {{
            SpringContextProvider.getApplicationContext().getBean("taskExecutor", TaskExecutor.class);
            result = taskExecutor;
        }};

        domibusQuartzThreadExecutor.execute(thread);

        new Verifications() {{
            taskExecutor.execute(thread);
        }};
    }
}
