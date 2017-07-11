package eu.domibus.common.services.impl;

import com.google.common.collect.Lists;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.dao.ProcessDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.validators.ProcessValidator;
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
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import javax.jms.Destination;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    @Spy
    private ProcessValidator processValidator;

    @InjectMocks
    private MessageExchangeServiceImpl messageExchangeService;
    private Process process;


    private Configuration configuration;
    private Party correctParty;

    @Before
    public void init() {
        correctParty = PojoInstaciatorUtil.instanciate(Party.class, " [name:party1]");
        process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "responderParties{[name:responder]}", "initiatorParties{[name:initiator]}");
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
        when(configurationDao.configurationExists()).thenReturn(true);
        when(configurationDao.read()).thenReturn(configuration);
        List<Process> processes = Lists.newArrayList(process);
        when(processDao.findPullProcessesByResponder(correctParty)).thenReturn(processes);
    }
    
    @Test
    public void testSuccessFullOneWayPullConfiguration() throws Exception {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:pull]","legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}","responderParties{[name:resp1]}");
        MessageStatus messageStatus = getMessageStatus(process);
        assertEquals(MessageStatus.READY_TO_PULL, messageStatus);
    }


    private MessageStatus getMessageStatus(Process process) throws EbMS3Exception {
        List<Process> processes= Lists.newArrayList();
        processes.add(process);
        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("agreementName", "senderParty", "receiverParty", "service", "action", "leg");
        when(processDao.findPullProcessesByMessageContext(messageExchangeConfiguration)).thenReturn(processes);
        return messageExchangeService.getMessageStatus(messageExchangeConfiguration);
    }

    @Test(expected = PModeException.class)
    public void testIncorrectMultipleProcessFoundForConfiguration() throws EbMS3Exception {
        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("agreementName", "senderParty", "receiverParty", "service", "action", "leg");
        List<Process> processes= Lists.newArrayList();
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:pull]");
        processes.add(process);
        process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:push]");
        processes.add(process);
        when(processDao.findPullProcessesByMessageContext(messageExchangeConfiguration)).thenReturn(processes);
        messageExchangeService.getMessageStatus(messageExchangeConfiguration);
    }

    @Test
    public void testInitiatePullRequest() throws Exception {
        ArgumentCaptor<Map> mapArgumentCaptor= ArgumentCaptor.forClass(Map.class);
        messageExchangeService.initiatePullRequest();
        verify(configurationDao,times(1)).configurationExists();
        verify(configurationDao,times(1)).read();
        verify(jmsPullTemplate,times(2)).convertAndSend(any(Destination.class),mapArgumentCaptor.capture(), any(MessagePostProcessor.class));
        //needed because the set does not return the values always in the same order.
        //@thom this does work on my machine but not on bamboo. Fix this.
        TestResult testResult = new TestResult("qn1", "party1:initiator:service1:Mock:Mock:leg1", "false");
        testResult.chain(new TestResult("qn2","party1:initiator:service2:Mock:Mock:leg2","false"));
        List<Map> allValues = mapArgumentCaptor.getAllValues();
        for (Map allValue : allValues) {
            assertTrue(testResult.testSucced(allValue));
        }
    }

    @Test
    public void testInitiatePullRequestWithoutConfiguration() throws Exception {
        when(configurationDao.configurationExists()).thenReturn(false);
        messageExchangeService.initiatePullRequest();
        verify(configurationDao,times(1)).configurationExists();
        verify(configurationDao,times(0)).read();
    }


    @Test
    public void testInvalidRequest() throws Exception {
        when(messageBuilder.buildSOAPMessage(any(SignalMessage.class),any(LegConfiguration.class))).thenThrow(
                new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "An error occurred while processing your request. Please check the message header for more details.", null,null));
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}","initiatorParties{[name:initiator]}");

        when(configurationDao.read()).thenReturn(configuration);
        List<Process> processes = Lists.newArrayList(process);
        when(processDao.findPullProcessesByResponder(correctParty)).thenReturn(processes);
        when(processDao.findPullProcessesByMessageContext(any(MessageExchangeConfiguration.class))).thenReturn(Lists.newArrayList(process));
        messageExchangeService.initiatePullRequest();
        verify(jmsPullTemplate,times(0)).convertAndSend(any(Destination.class),any(Map.class), any(MessagePostProcessor.class));
    }

    @Test
    public void extractProcessOnMpc() throws Exception {
        List<Process> processes = Lists.newArrayList(PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:pull]", "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "responderParties{[name:resp1]}"));
        when(processDao.findPullProcessBytMpc(eq("qn1"))).thenReturn(processes);
        PullContext pullContext = messageExchangeService.extractProcessOnMpc("qn1");
        assertEquals(true, pullContext.isValid());
        assertEquals("resp1",pullContext.getResponder().getName());
        assertEquals("party1",pullContext.getInitiator().getName());
        assertEquals("oneway",pullContext.getProcess().getMep().getName());
    }

    @Test
    public void extractProcessMpcWithNoProcess() throws Exception {
        when(processDao.findPullProcessBytMpc(eq("qn1"))).thenReturn(new ArrayList<Process>());
        PullContext pullContext = messageExchangeService.extractProcessOnMpc("qn1");
        assertEquals(false, pullContext.isValid());
    }

    @Test
    public void extractProcessMpcWithNoToManyProcess() throws Exception {
        when(processDao.findPullProcessBytMpc(eq("qn1"))).thenReturn(Lists.newArrayList(new Process(), new Process()));
        PullContext pullContext = messageExchangeService.extractProcessOnMpc("qn1");
        assertEquals(false, pullContext.isValid());
    }





}