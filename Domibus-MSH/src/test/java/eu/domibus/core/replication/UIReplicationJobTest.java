package eu.domibus.core.replication;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class UIReplicationJobTest {

    @Tested
    UIReplicationJob uiReplicationJob;

    @Injectable
    private UIMessageDiffService uiMessageDiffService;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Test
    public void testExecuteJob_UIReplicationEnabled(final @Mocked JobExecutionContext jobExecutionContext, final @Mocked Domain domain) throws Exception {

        new Expectations() {{
            uiReplicationSignalService.isReplicationEnabled();
            result = true;
        }};

        //tested method
        uiReplicationJob.executeJob(jobExecutionContext, domain);

        new FullVerifications() {{
            uiMessageDiffService.findAndSyncUIMessages();
        }};
    }

    @Test
    public void testExecuteJob_UIReplicationDisabled(final @Mocked JobExecutionContext jobExecutionContext, final @Mocked Domain domain) throws Exception {

        new Expectations() {{
            uiReplicationSignalService.isReplicationEnabled();
            result = false;
        }};

        //tested method
        uiReplicationJob.executeJob(jobExecutionContext, domain);

        new FullVerifications() {{
        }};
    }
}