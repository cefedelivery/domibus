package eu.domibus.common.services.impl;

import com.google.common.collect.Lists;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.dao.ProcessDao;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Created by dussath on 5/24/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagePullerServiceTest {

    @Mock
    private ProcessDao processDao;
    @Mock
    private ConfigurationDAO configurationDao;
    @Mock
    private MSHDispatcher mshDispatcher;
    @Mock
    private EbMS3MessageBuilder messageBuilder;
    @InjectMocks
    private MessagePullerServiceImpl messagePullerServiceImpl;
    private static Process process;


    @Before
    public void init() {
        Party correctParty = PojoInstaciatorUtil.instanciate(Party.class, "[name:party1]");
        process = PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1,action[name:action]]}", "responderParties{[name:endPoint1];[name:endPoint2]}");
        LegConfiguration uniqueLeg = process.getLegs().iterator().next();
        Service service = new Service();
        service.setName("service");
        uniqueLeg.setService(service);
        Mpc mpc = new Mpc();
        mpc.setName("mpcName");
        uniqueLeg.setDefaultMpc(mpc);
        Configuration configuration = new Configuration();
        configuration.setParty(correctParty);
        when(configurationDao.read()).thenReturn(configuration);
        List<Process> processes = Lists.newArrayList(process);
        when(processDao.findPullProcessesByIniator(correctParty)).thenReturn(processes);
    }

    
}