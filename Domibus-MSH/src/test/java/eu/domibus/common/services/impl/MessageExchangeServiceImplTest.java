package eu.domibus.common.services.impl;

import com.google.common.collect.Lists;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ProcessDao;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeContext;
import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by dussath on 5/19/17.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageExchangeServiceImplTest {

    @Mock
    private ProcessDao processDao;
    @InjectMocks
    private MessageExchangeServiceImpl messagingService;
    @Test
    public void testSuccessFullOneWayPullConfiguration() throws Exception {

        Process process = PojoInstaciatorUtil.instanciateProcess(Process.class, "mep/name:oneway", "mepBinding/name:pull");
        MessageExchangeContext messageExchangeContext = getMessageExchangeContext(process);
        assertEquals(MessageStatus.READY_TO_PULL,messageExchangeContext.getMessageStatus());

    }

    @Test
    public void testOneWayPullOnlySupported(){
        Process process = PojoInstaciatorUtil.instanciateProcess(Process.class, "mep/name:twoway", "mepBinding/name:pull");
        try {
            getMessageExchangeContext(process);
            assertTrue(false);
        }catch (RuntimeException e){

        }
    }

    private MessageExchangeContext getMessageExchangeContext(Process process) {
        List<Process> processes= Lists.newArrayList();
        processes.add(process);
        MessageExchangeContext messageExchangeContext = new MessageExchangeContext("agreementName", "senderParty", "receiverParty", "service", "action", "leg");
        when(processDao.findProcessForMessageContext(messageExchangeContext)).thenReturn(processes);
        messagingService.upgradeMessageExchangeStatus(messageExchangeContext);
        return messageExchangeContext;
    }

    @Test
    public void testIncorrectMultipleProcessFoundForConfiguration(){
        MessageExchangeContext messageExchangeContext = new MessageExchangeContext("agreementName", "senderParty", "receiverParty", "service", "action", "leg");
        List<Process> processes= Lists.newArrayList();
        Process process = PojoInstaciatorUtil.instanciateProcess(Process.class, "mep/name:oneway", "mepBinding/name:pull");
        processes.add(process);
        process = PojoInstaciatorUtil.instanciateProcess(Process.class, "mep/name:oneway", "mepBinding/name:push");
        processes.add(process);
        when(processDao.findProcessForMessageContext(messageExchangeContext)).thenReturn(processes);
        try {
            messagingService.upgradeMessageExchangeStatus(messageExchangeContext);
            assertTrue(false);
        }catch (RuntimeException e){

        }
    }


}