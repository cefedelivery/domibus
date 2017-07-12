package eu.domibus.common.dao;

import eu.domibus.AbstractIT;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Thomas Dussart
 * @since 3.3
 * Testing class for ProcessDao
 */
@ContextConfiguration("classpath:pmode-dao.xml")
public class ProcessDaoImplTestIT extends AbstractIT{
    @Autowired
    private PModeDao pModeDao;
    @Autowired
    private ProcessDao processDao;


    @Test
    @Transactional
    @Rollback
    public void findProcessForMessageContext() throws Exception {
        loadBluePullPmodeFile();
        List<Process> processesForMessageContext = processDao.findPullProcessesByMessageContext(new MessageExchangeConfiguration("", "blue_gw", "red_gw", "pullService", "pullAction", "pullLeg2"));
        assertEquals(1,processesForMessageContext.size());
        Process process = processesForMessageContext.get(0);
        assertNull(process.getAgreement());
        assertEquals("pull", process.getMepBinding().getName());
        assertEquals("oneway",process.getMep().getName());
    }

    @Test
    @Transactional
    @Rollback
    public void findPullByResponder() throws Exception {
        loadBluePullPmodeFile();
        Party party = pModeDao.getReceiverParty(":red_gw");
        List<Process> pullProcessesByIniator = processDao.findPullProcessesByResponder(party);
        assertEquals(1,pullProcessesByIniator.size());

        Process process = pullProcessesByIniator.get(0);
        assertNull(process.getAgreement());
        assertEquals("pull",process.getMepBinding().getName());
        assertEquals("oneway",process.getMep().getName());
    }

    private void loadBluePullPmodeFile() throws XmlProcessingException, IOException, URISyntaxException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("samplePModes/domibus-configuration-blue-pull.xml");
        pModeDao.updatePModes(IOUtils.toByteArray(inputStream));

    }

    @Test
    @Transactional
    @Rollback
    public void findPullProcessFromRequestMpc() throws IOException, XmlProcessingException, URISyntaxException {
        loadBluePullPmodeFile();
        List<Process> pullProcessFromRequestPartyAndMpc = processDao.findPullProcessBytMpc("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC");
        assertEquals(1,pullProcessFromRequestPartyAndMpc.size());
    }


}