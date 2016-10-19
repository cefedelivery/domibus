package eu.domibus.wss4j.common.crypto;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
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
import java.io.IOException;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:eu/domibus/wss4j/common/crypto/CryptoServiceTest/CryptoServiceTest-context.xml")
@DirtiesContext
public class CryptoServiceTest {

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/wss4j/common/crypto/CryptoServiceTest/";
    private static final String NON_EMPTY_SOURCE_KEYSTORE = "nonEmptySource.jks";
    private static final String NON_EMPTY_TARGET_KEYSTORE = "nonEmptyTarget.jks";
    private static final String EMPTY_SOURCE_KEYSTORE = "emptySource.jks";
    private static final String EMPTY_TARGET_KEYSTORE = "emptyTarget.jks";
    private static final String TRUSTSTORE_FILE_PROPERTY_NAME = "org.apache.ws.security.crypto.merlin.trustStore.file";
    private static final String TRUSTSTORE_PASSWORD_PROPERTY_NAME = "org.apache.ws.security.crypto.merlin.trustStore.password";
    private static final String TRUSTSTORE_PASSWORD_PROPERTY_VALUE = "1234";

    private static boolean initialized;

    @InjectMocks
    private CryptoService classUnderTest;

    @Mock
    private JmsOperations jmsOperations;

    @Spy
    private Properties trustStoreProperties;

    @BeforeClass
    public static void init() throws IOException {
        if (!initialized) {
            FileUtils.deleteDirectory(new File("target/temp"));
            System.setProperty("domibus.config.location", new File("target/test-classes").getAbsolutePath());
            initialized = true;
        }
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    //@Test TODO fix it
    public void testReplaceTruststore_NonEmptyTruststore_ContentOfNewTruststoreExpected() throws Exception {
        doReturn(RESOURCE_PATH + NON_EMPTY_TARGET_KEYSTORE).when(trustStoreProperties).getProperty(TRUSTSTORE_FILE_PROPERTY_NAME);
        doReturn(TRUSTSTORE_PASSWORD_PROPERTY_VALUE).when(trustStoreProperties).getProperty(TRUSTSTORE_PASSWORD_PROPERTY_NAME);

        byte[] sourceKeyStore = FileUtils.readFileToByteArray(new File(RESOURCE_PATH + NON_EMPTY_SOURCE_KEYSTORE));

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


}