package eu.domibus.common.dao;

import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeContext;
import eu.domibus.ebms3.common.dao.DefaultDaoTestConfiguration;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.xml.XMLUtilImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsOperations;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by dussath on 5/18/17.
 * Testing class for ProcessDao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ProcessDaoImplTestIt{


    static class ContextConfiguration extends DefaultDaoTestConfiguration {
        @Bean
        public PModeDao pModeProvider(){return new PModeDao();}
        @Bean
        public ProcessDao processDao(){return new ProcessDaoImpl();}
        @Bean
        public ConfigurationDAO configurationDAO(){return new ConfigurationDAO();}

        @Bean
        @Qualifier("jaxbContextConfig")
        public JAXBContext jaxbContext(){
            try {
                return JAXBContext.newInstance("eu.domibus.common.model.configuration");
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }

        @Bean
        @Qualifier("jmsTemplateCommand")
        public JmsOperations jmsOperations(){return Mockito.mock(JmsOperations.class);}
        @Bean
        public XMLUtil xmlUtil(){return new XMLUtilImpl();}
    }
    @Autowired
    private PModeDao pModeProvider;
    @Autowired
    private ProcessDao processDao;
    @Autowired
    private ConfigurationDAO configurationDAO;
    @Test
    @Transactional
    @Rollback
    public void findProcessForMessageContext() throws Exception {
        File pModeFile = new File("src/test/resources/SamplePModes/domibus-configuration-valid.xml");
        FileInputStream fis = new FileInputStream(pModeFile);
        pModeProvider.updatePModes(IOUtils.toByteArray(fis));

        List<Process> processesForMessageContext = processDao.findProcessForMessageContext(new MessageExchangeContext("agreement1110", "blue_gw", "red_gw", "noSecService", "noSecAction", "pushNoSecnoSecAction"));
        assertEquals(1,processesForMessageContext.size());
        Process process = processesForMessageContext.get(0);
        assertEquals("agreement1110",process.getAgreement().getName());
        assertEquals("push",process.getMepBinding().getName());
        assertEquals("oneway",process.getMep().getName());

        processesForMessageContext = processDao.findProcessForMessageContext(new MessageExchangeContext("agreement1110", "domibus_de", "ibmgw", "testService3", "tc3ActionLeg1", "pushTestcase3Leg1tc3ActionLeg1"));
        assertEquals(1,processesForMessageContext.size());
        process = processesForMessageContext.get(0);
        assertEquals("agreement1110",process.getAgreement().getName());
        assertEquals("pushAndPush",process.getMepBinding().getName());
        assertEquals("twoway",process.getMep().getName());
    }

    @Test
    @Transactional
    @Rollback
    public void findPullProcessesByIniator() throws Exception {
        File pModeFile = new File("src/test/resources/SamplePModes/domibus-configuration-blue-pull.xml");
        FileInputStream fis = new FileInputStream(pModeFile);
        pModeProvider.updatePModes(IOUtils.toByteArray(fis));
        Configuration configuration = configurationDAO.read();
        List<Process> pullProcessesByIniator = processDao.findPullProcessesByIniator(configuration.getParty());
        assertEquals(1,pullProcessesByIniator.size());

        Process process = pullProcessesByIniator.get(0);
        assertNull(process.getAgreement());
        assertEquals("pull",process.getMepBinding().getName());
        assertEquals("oneway",process.getMep().getName());
    }

    @Test
    @Transactional
    @Rollback
    public void findPullProcessFromRequestPartyAndMpc() throws IOException, XmlProcessingException {
        File pModeFile = new File("src/test/resources/SamplePModes/domibus-configuration-blue-pull.xml");
        FileInputStream fis = new FileInputStream(pModeFile);
        pModeProvider.updatePModes(IOUtils.toByteArray(fis));
        List<Process> pullProcessFromRequestPartyAndMpc = processDao.findPullProcessFromRequestPartyAndMpc("red_gw", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC");
        assertEquals(1,pullProcessFromRequestPartyAndMpc.size());
    }


}