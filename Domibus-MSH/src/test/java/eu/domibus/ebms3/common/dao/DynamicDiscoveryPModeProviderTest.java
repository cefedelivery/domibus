package eu.domibus.ebms3.common.dao;

import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Process;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import java.io.File;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/DynamicDiscoveryPModeProviderTest-context.xml")
@DirtiesContext
public class DynamicDiscoveryPModeProviderTest {

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
    private static final String DYNRESPONDER_AND_PARTYSELF = "dynResponderAndPartySelf.xml";

    @Autowired
    private JAXBContext jaxbConfigurationObjectContext;


    @Test
    public void testFindDynamicReceiverProcesses_DynResponderAndPartySelf_ProcessInResultExpected() throws Exception {
        Configuration testData = (Configuration)jaxbConfigurationObjectContext.createUnmarshaller().unmarshal(new File(RESOURCE_PATH + DYNRESPONDER_AND_PARTYSELF));

        DynamicDiscoveryPModeProvider classUnderTest = mock(DynamicDiscoveryPModeProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(testData).when(classUnderTest).getConfiguration();

        Collection<Process> result = classUnderTest.findDynamicReceiverProcesses();

        assertEquals(1, result.size());

    }

    @Test
    public void testDoDynamicThings() throws Exception {

    }

    @Test
    public void testExtractCommonName() throws Exception {

    }
}