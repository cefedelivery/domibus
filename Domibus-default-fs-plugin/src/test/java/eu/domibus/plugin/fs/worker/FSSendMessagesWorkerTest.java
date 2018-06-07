package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
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

    @Injectable
    private DomainExtService domainExtService;

    @Injectable
    private DomainContextExtService domainContextExtService;

    @Tested
    private FSSendMessagesWorker sendMessagesWorker;

    @Test
    public void testExecuteJob(@Injectable final JobExecutionContext context) {
        sendMessagesWorker.executeJob(context, null);

        new VerificationsInOrder(1){{
            sendMessagesService.sendMessages();
        }};
    }

}