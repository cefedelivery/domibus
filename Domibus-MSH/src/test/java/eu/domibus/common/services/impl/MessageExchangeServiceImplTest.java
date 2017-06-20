package eu.domibus.common.services.impl;

import com.google.common.collect.Lists;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.dao.ProcessDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import javax.jms.Destination;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageExchangeServiceImplTest {

    @Mock
    private ProcessDao processDao;
    @Mock
    private ConfigurationDAO configurationDao;
    @Mock
    private JmsTemplate jmsPullTemplate;
    @Mock
    private EbMS3MessageBuilder messageBuilder;
    @InjectMocks
    private MessageExchangeServiceImpl messageExchangeService;
    private Process process;

    private Configuration configuration;
    private Party correctParty;

    @Before
    public void init() {
        correctParty = PojoInstaciatorUtil.instanciate(Party.class, " [name:party1]");
        process = PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}","responderParties{[name:responder]}","initiatorParties{[name:initiator]}");
        Iterator<LegConfiguration> iterator = process.getLegs().iterator();
        LegConfiguration firstLeg = iterator.next();
        Service service = new Service();
        service.setName("service1");
        firstLeg.setService(service);

        LegConfiguration secondLeg = iterator.next();
        service = new Service();
        service.setName("service2");
        secondLeg.setService(service);

        configuration = new Configuration();
        configuration.setParty(correctParty);
        when(configurationDao.read()).thenReturn(configuration);
        List<Process> processes = Lists.newArrayList(process);
        when(processDao.findPullProcessesByResponder(correctParty)).thenReturn(processes);
    }
    
    @Test
    public void testSuccessFullOneWayPullConfiguration() throws Exception {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:pull]","legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}","responderParties{[name:resp1]}");
        MessageExchangeConfiguration messageExchangeConfiguration = getMessageExchangeContext(process);
        assertEquals(MessageStatus.READY_TO_PULL, messageExchangeConfiguration.getMessageStatus());
    }

    @Test
    public void testOneWayPullOnlySupported(){
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:twoway]", "mepBinding[name:pull]");
        try {
            getMessageExchangeContext(process);
            assertTrue(false);
        }catch (RuntimeException e){

        }
    }

    private MessageExchangeConfiguration getMessageExchangeContext(Process process) {
        List<Process> processes= Lists.newArrayList();
        processes.add(process);
        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("agreementName", "senderParty", "receiverParty", "service", "action", "leg");
        when(processDao.findProcessByMessageContext(messageExchangeConfiguration)).thenReturn(processes);
        messageExchangeService.upgradeMessageExchangeStatus(messageExchangeConfiguration);
        return messageExchangeConfiguration;
    }

    @Test
    public void testIncorrectMultipleProcessFoundForConfiguration(){
        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("agreementName", "senderParty", "receiverParty", "service", "action", "leg");
        List<Process> processes= Lists.newArrayList();
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:pull]");
        processes.add(process);
        process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:push]");
        processes.add(process);
        when(processDao.findProcessByMessageContext(messageExchangeConfiguration)).thenReturn(processes);
        try {
            messageExchangeService.upgradeMessageExchangeStatus(messageExchangeConfiguration);
            assertTrue(false);
        }catch (RuntimeException e){

        }
    }

    @Test
    public void testInitiatePullRequest() throws Exception {
        ArgumentCaptor<Map> mapArgumentCaptor= ArgumentCaptor.forClass(Map.class);
        messageExchangeService.initiatePullRequest();
        verify(jmsPullTemplate,times(2)).convertAndSend(any(Destination.class),mapArgumentCaptor.capture(), any(MessagePostProcessor.class));
        //needed because the set does not return the values always in the same order.
        TestResult testResult = new TestResult("qn1", "party1:initiator:service1:Mock:Mock:leg1", "false");
        testResult.chain(new TestResult("qn2","party1:initiator:service2:Mock:Mock:leg2","false"));
        List<Map> allValues = mapArgumentCaptor.getAllValues();
        for (Map allValue : allValues) {
            assertTrue(testResult.testSucced(allValue));
        }

    }

    @Test
    public void testInvalidRequest() throws Exception {
        when(messageBuilder.buildSOAPMessage(any(SignalMessage.class),any(LegConfiguration.class))).thenThrow(
                new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "An error occurred while processing your request. Please check the message header for more details.", null,null));
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}","initiatorParties{[name:initiator]}");

        when(configurationDao.read()).thenReturn(configuration);
        List<Process> processes = Lists.newArrayList(process);
        when(processDao.findPullProcessesByResponder(correctParty)).thenReturn(processes);
        when(processDao.findProcessByMessageContext(any(MessageExchangeConfiguration.class))).thenReturn(Lists.newArrayList(process));
        messageExchangeService.initiatePullRequest();
        verify(jmsPullTemplate,times(0)).convertAndSend(any(Destination.class),any(Map.class), any(MessagePostProcessor.class));
    }

    @Test
    public void extractProcessOnMpc() throws Exception {
        List<Process> processes = Lists.newArrayList(PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:pull]", "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "responderParties{[name:resp1]}"));
        when(processDao.findPullProcessBytMpc(eq("qn1"))).thenReturn(processes);
        PullContext pullContext = messageExchangeService.extractProcessOnMpc("qn1");
        assertEquals("resp1",pullContext.getResponder().getName());
        assertEquals("party1",pullContext.getInitiator().getName());
        assertEquals("oneway",pullContext.getProcess().getMep().getName());
    }





}