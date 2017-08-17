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
public class FSPurgeSentWorkerTest {

    @Injectable
    private FSPurgeSentService purgeSentService;

    @Tested
    private FSPurgeSentWorker purgeSentWorker;

    @Test
    public void testExecuteInternal(@Injectable final JobExecutionContext context) throws Exception {
        purgeSentWorker.executeInternal(context);

        new VerificationsInOrder(1){{
            purgeSentService.purgeMessages();
        }};
    }

}