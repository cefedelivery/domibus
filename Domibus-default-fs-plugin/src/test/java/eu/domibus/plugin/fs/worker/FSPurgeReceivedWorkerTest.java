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
public class FSPurgeReceivedWorkerTest {

    @Injectable
    private FSPurgeReceivedService purgeReceivedService;

    @Tested
    private FSPurgeReceivedWorker purgeReceivedWorker;

    @Injectable
    private DomainExtService domainExtService;

    @Injectable
    private DomainContextExtService domainContextExtService;

    @Test
    public void testExecuteJob(@Injectable final JobExecutionContext context) throws Exception {
        purgeReceivedWorker.executeJob(context, null);

        new VerificationsInOrder(1){{
            purgeReceivedService.purgeMessages();
        }};
    }

}