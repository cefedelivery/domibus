package eu.domibus.plugin.fs.worker;

import mockit.Injectable;
import mockit.Tested;
import mockit.VerificationsInOrder;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@RunWith(JMockit.class)
public class FSSendMessagesWorkerTest {

    @Injectable
    private FSSendMessagesService sendMessagesService;

    @Tested
    private FSSendMessagesWorker sendMessagesWorker;

    @Test
    public void testExecuteInternal(@Injectable final JobExecutionContext context) throws Exception {
        sendMessagesWorker.executeInternal(context);

        new VerificationsInOrder(1){{
            sendMessagesService.sendMessages();
        }};
    }

}