package eu.domibus.ebms3.common.dao;

import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Process;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/DynamicDiscoveryPModeProviderTest-context.xml")
@DirtiesContext
public class DynamicDiscoveryPModeProviderTest {

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
    private static final String DYNRESPONDER_AND_PARTYSELF = "dynResponderAndPartySelf.xml";
    private static final String MULTIPLE_DYNRESPONDER_AND_PARTYSELF = "multipleDynResponderAndPartySelf.xml";
    private static final String MULTIPLE_DYNINITIATOR_AND_PARTYSELF = "multipleDynInitiatorAndPartySelf.xml";
    private static final String MULTIPLE_DYNRESPONDER_AND_DYNINITIATOR = "multipleDynResponderAndInitiator.xml";

    private static final String TEST_KEYSTORE = "testkeystore.jks";

    private static final String EXPECTED_DYNAMIC_PROCESS_NAME = "testProcessDynamicExpected";
    private static final String UNEXPECTED_DYNAMIC_PROCESS_NAME = "testProcessStaticNotExpected";

    private static final String EXPECTED_COMMON_NAME = "DONOTUSE_TEST";

    private static final String ALIAS_CN_AVAILABLE = "cn_available";
    private static final String ALIAS_CN_NOT_AVAILABLE = "cn_not_available";

    @Autowired
    private JAXBContext jaxbConfigurationObjectContext;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void testFindDynamicReceiverProcesses_DynResponderAndPartySelf_ProcessInResultExpected() throws Exception {
        Configuration testData = (Configuration)jaxbConfigurationObjectContext.createUnmarshaller().unmarshal(new File(RESOURCE_PATH + DYNRESPONDER_AND_PARTYSELF));
        assertTrue(initializeConfiguration(testData));

        DynamicDiscoveryPModeProvider classUnderTest = mock(DynamicDiscoveryPModeProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(testData).when(classUnderTest).getConfiguration();

        Collection<Process> result = classUnderTest.findDynamicReceiverProcesses();

        assertEquals(1, result.size());

        Process foundProcess = result.iterator().next();
        assertTrue(foundProcess.isDynamicResponder());
        assertEquals(EXPECTED_DYNAMIC_PROCESS_NAME, foundProcess.getName());
    }

    @Test
    public void testFindDynamicReceiverProcesses_MultipleDynResponderAndPartySelf_MultipleProcessesInResultExpected() throws Exception {
        Configuration testData = (Configuration)jaxbConfigurationObjectContext.createUnmarshaller().unmarshal(new File(RESOURCE_PATH + MULTIPLE_DYNRESPONDER_AND_PARTYSELF));
        assertTrue(initializeConfiguration(testData));

        DynamicDiscoveryPModeProvider classUnderTest = mock(DynamicDiscoveryPModeProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(testData).when(classUnderTest).getConfiguration();

        Collection<Process> result = classUnderTest.findDynamicReceiverProcesses();

        assertEquals(3, result.size());

        for(Process process : result) {
            assertTrue(process.isDynamicResponder());
            assertNotEquals(UNEXPECTED_DYNAMIC_PROCESS_NAME, process.getName());
        }
    }

    @Test
    public void testFindDynamicReceiverProcesses_MultipleDynInitiatorAndPartySelf_NoProcessesInResultExpected() throws Exception {
        Configuration testData = (Configuration)jaxbConfigurationObjectContext.createUnmarshaller().unmarshal(new File(RESOURCE_PATH + MULTIPLE_DYNINITIATOR_AND_PARTYSELF));
        assertTrue(initializeConfiguration(testData));

        DynamicDiscoveryPModeProvider classUnderTest = mock(DynamicDiscoveryPModeProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(testData).when(classUnderTest).getConfiguration();

        Collection<Process> result = classUnderTest.findDynamicReceiverProcesses();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindDynamicReceiverProcesses_MultipleDynResponderAndDynInitiator_MultipleInResultExpected() throws Exception {
        Configuration testData = (Configuration)jaxbConfigurationObjectContext.createUnmarshaller().unmarshal(new File(RESOURCE_PATH + MULTIPLE_DYNRESPONDER_AND_DYNINITIATOR));
        assertTrue(initializeConfiguration(testData));

        DynamicDiscoveryPModeProvider classUnderTest = mock(DynamicDiscoveryPModeProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(testData).when(classUnderTest).getConfiguration();

        Collection<Process> result = classUnderTest.findDynamicReceiverProcesses();

        assertEquals(3, result.size());

        for(Process process : result) {
            assertTrue(process.isDynamicInitiator());
            assertTrue(process.isDynamicResponder());
        }
    }

    @Test
    public void testDoDynamicThings() throws Exception {

    }

    @Test
    public void testExtractCommonName_PublicKeyWithCommonNameAvailable_CorrectCommonNameExpected() throws Exception {

        X509Certificate testData = loadCertificateFromPem(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE);
        assertNotNull(testData);

        DynamicDiscoveryPModeProvider classUnderTest = new DynamicDiscoveryPModeProvider();

        String result = classUnderTest.extractCommonName(testData);

        assertEquals(EXPECTED_COMMON_NAME, result);
    }

    @Test
    public void testExtractCommonName_PublicKeyWithCommonNameNotAvailable_IllegalArgumentExceptionExpected() throws Exception{
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(startsWith("The certificate does not contain a common name (CN): "));

        X509Certificate testData = loadCertificateFromPem(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_NOT_AVAILABLE);
        assertNotNull(testData);

        DynamicDiscoveryPModeProvider classUnderTest = new DynamicDiscoveryPModeProvider();

        classUnderTest.extractCommonName(testData);

    }

    /**
     * Call private method {@code Configuration#preparePersist} in order to initialize the configuration object properly
     *
     * @param configuration
     * @return
     */
    private boolean initializeConfiguration(Configuration configuration) {
        try {
            Method preparePersist = configuration.getClass().getDeclaredMethod("preparePersist");
            preparePersist.setAccessible(true);
            preparePersist.invoke(configuration);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException | NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    /**
     * Load certificate with alias from JKS and return as {@code X509Certificate}.
     * The password is always 1234 in this test.
     *
     * @param filePath
     * @param alias
     * @return
     */
    private X509Certificate loadCertificateFromPem(String filePath, String alias) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, "1234".toCharArray());

            Certificate cert = keyStore.getCertificate(alias);

            return (X509Certificate)cert;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}