package eu.domibus.wss4j.common.crypto;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.jms.core.JmsOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:eu/domibus/wss4j/common/crypto/CryptoServiceTest/CryptoServiceTest-context.xml")
@DirtiesContext
public class CryptoServiceTest {

    private static final Log LOG = LogFactory.getLog(CryptoServiceTest.class);

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/wss4j/common/crypto/CryptoServiceTest/";

    private static final String NON_EMPTY_SOURCE_KEYSTORE = "nonEmptySource.jks";
    private static final String NON_EMPTY_TARGET_KEYSTORE = "nonEmptyTarget.jks";
    private static final String GREEN_KEYSTORE = "green_gw_keystore.jks";

    private static final String TRUSTSTORE_FILE_PROPERTY_NAME = "org.apache.ws.security.crypto.merlin.trustStore.file";
    private static final String TRUSTSTORE_PASSWORD_PROPERTY_NAME = "org.apache.ws.security.crypto.merlin.trustStore.password";
    private static final String TRUSTSTORE_PASSWORD_PROPERTY_VALUE = "1234";

    private static final String KEYSTORE_PASSWORD_PROPERTY_VALUE = "test123";
    private static final String KEYSTORE_FILE_PROPERTY_NAME = "org.apache.ws.security.crypto.merlin.file";
    private static final String KEYSTORE_PASSWORD = "org.apache.ws.security.crypto.merlin.keystore.password";

    @InjectMocks
    private CryptoService classUnderTest;

    @Mock
    private JmsOperations jmsOperations;

    @Spy
    private Properties trustStoreProperties;

    @Spy
    private Properties keyStoreProperties;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReplaceTruststore_NonEmptyTruststore_ContentOfNewTruststoreExpected() throws Exception {

        when(trustStoreProperties.getProperty(TRUSTSTORE_FILE_PROPERTY_NAME)).thenReturn(RESOURCE_PATH + NON_EMPTY_TARGET_KEYSTORE);
        when(trustStoreProperties.getProperty(TRUSTSTORE_PASSWORD_PROPERTY_NAME)).thenReturn(TRUSTSTORE_PASSWORD_PROPERTY_VALUE);

        byte[] sourceKeyStore = FileUtils.readFileToByteArray(new File(RESOURCE_PATH + NON_EMPTY_SOURCE_KEYSTORE));

        classUnderTest.setTrustStoreProperties(trustStoreProperties);
        classUnderTest.replaceTruststore(sourceKeyStore, "1234");

        FileInputStream expectedKeyStoreInputStream = new FileInputStream(RESOURCE_PATH + NON_EMPTY_SOURCE_KEYSTORE);

        KeyStore expectedKeyStore = KeyStore.getInstance("JKS");
        expectedKeyStore.load(expectedKeyStoreInputStream, TRUSTSTORE_PASSWORD_PROPERTY_VALUE.toCharArray());

        KeyStore result = classUnderTest.getTrustStore();

        assertNotNull(result);
        assertEquals(expectedKeyStore.size(), result.size());

        Enumeration<String> aliases = expectedKeyStore.aliases();
        do {
            assertTrue(result.containsAlias(aliases.nextElement()));
        } while (aliases.hasMoreElements());

    }

    @Test
    public void testGetCertificateFromKeystoreOk() throws Exception {

        when(keyStoreProperties.getProperty(KEYSTORE_FILE_PROPERTY_NAME)).thenReturn(RESOURCE_PATH + GREEN_KEYSTORE);
        when(keyStoreProperties.getProperty(KEYSTORE_PASSWORD)).thenReturn(KEYSTORE_PASSWORD_PROPERTY_VALUE);

        classUnderTest.setKeyStoreProperties(keyStoreProperties);

        String alias = "green_gw";
        Certificate certificate = classUnderTest.getCertificateFromKeystore(alias);
        Assert.assertNotNull(certificate);
    }

    @Test
    public void testGetCertificateFromKeystoreNOk() {

        when(keyStoreProperties.getProperty(KEYSTORE_FILE_PROPERTY_NAME)).thenReturn(RESOURCE_PATH + GREEN_KEYSTORE);
        when(keyStoreProperties.getProperty(KEYSTORE_PASSWORD)).thenReturn("123");

        classUnderTest.setKeyStoreProperties(keyStoreProperties);

        String alias = "green_gw";
        try {
            classUnderTest.getCertificateFromKeystore(alias);
        } catch (KeyStoreException ksEx) {
            LOG.info("KeyStoreException correctly raised:", ksEx);
        }
    }

}