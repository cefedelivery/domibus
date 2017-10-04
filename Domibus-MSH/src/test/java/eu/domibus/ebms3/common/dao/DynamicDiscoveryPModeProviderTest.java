package eu.domibus.ebms3.common.dao;

import eu.domibus.api.util.xml.UnmarshallerResult;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.dao.ProcessDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.services.impl.DynamicDiscoveryServiceOASIS;
import eu.domibus.common.services.impl.DynamicDiscoveryServicePEPPOL;
import eu.domibus.common.util.EndpointInfo;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.Service;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.pki.CertificateServiceImpl;
import eu.domibus.wss4j.common.crypto.CryptoService;
import eu.domibus.xml.XMLUtilImpl;
import eu.europa.ec.dynamicdiscovery.model.Endpoint;
import eu.europa.ec.dynamicdiscovery.model.ProcessIdentifier;
import eu.europa.ec.dynamicdiscovery.model.TransportProfile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.xml.bind.JAXBContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DynamicDiscoveryPModeProviderTest {

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
    private static final String DYNRESPONDER_AND_PARTYSELF = "dynResponderAndPartySelf.xml";
    private static final String MULTIPLE_DYNRESPONDER_AND_PARTYSELF = "multipleDynResponderAndPartySelf.xml";
    private static final String MULTIPLE_DYNINITIATOR_AND_PARTYSELF = "multipleDynInitiatorAndPartySelf.xml";
    private static final String MULTIPLE_DYNRESPONDER_AND_DYNINITIATOR = "multipleDynResponderAndInitiator.xml";
    private static final String NO_DYNINITIATOR_AND_NOT_SELF = "noDynInitiatorAndNotPartySelf.xml";
    private static final String DYNAMIC_DISCOVERY_ENABLED = "dynamicDiscoveryEnabled.xml";

    private static final String TEST_KEYSTORE = "testkeystore.jks";

    private static final String EXPECTED_DYNAMIC_PROCESS_NAME = "testProcessDynamicExpected";
    private static final String UNEXPECTED_DYNAMIC_PROCESS_NAME = "testProcessStaticNotExpected";

    private static final String EXPECTED_COMMON_NAME = "DONOTUSE_TEST";

    private static final String ALIAS_CN_AVAILABLE = "cn_available";
    private static final String ALIAS_CN_NOT_AVAILABLE = "cn_not_available";
    private static final String CERT_PASSWORD = "1234";

    private static final String TEST_ACTION_VALUE = "testAction";
    private static final String TEST_SERVICE_VALUE = "serviceValue";
    private static final String TEST_SERVICE_TYPE = "serviceType";
    private static final String UNKNOWN_DYNAMIC_RESPONDER_PARTYID_VALUE = "unkownResponderPartyIdValue";
    private static final String UNKNOWN_DYNAMIC_RESPONDER_PARTYID_TYPE = "unkownResponderPartyIdType";
    private static final String UNKNOWN_DYNAMIC_INITIATOR_PARTYID_VALUE = "unknownInitiatorPartyIdValue";
    private static final String UNKNOWN_DYNAMIC_INITIATOR_PARTYID_TYPE = "unknownInitiatorPartyIdType";

    private static final String PROCESSIDENTIFIER_ID = "testIdentifierId";
    private static final String PROCESSIDENTIFIER_SCHEME = "testIdentifierScheme";
    private static final String ADDRESS = "http://localhost:9090/anonymous/msh";

    @Mock
    private DynamicDiscoveryServicePEPPOL dynamicDiscoveryServicePEPPOL;

    @Mock
    private ProcessDao processDao;

    @Mock
    private DynamicDiscoveryServiceOASIS dynamicDiscoveryServiceOASIS;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Spy
    private CertificateServiceImpl certificateService;

    @InjectMocks
    private DynamicDiscoveryPModeProvider dynamicDiscoveryPModeProvider;

    @Spy
    ConfigurationDAO configurationDAO;

    @Mock
    CryptoService cryptoService;

    @Spy
    private Properties domibusProperties;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    private Configuration initializeConfiguration(String resourceXML) throws Exception {
        InputStream xmlStream = new FileInputStream(new File(RESOURCE_PATH + resourceXML));
        JAXBContext jaxbContext = JAXBContext.newInstance("eu.domibus.common.model.configuration");
        XMLUtil xmlUtil = new XMLUtilImpl();

        UnmarshallerResult unmarshallerResult = xmlUtil.unmarshal(false, jaxbContext, xmlStream, null);
        assertNotNull(unmarshallerResult.getResult());
        assertTrue(unmarshallerResult.getResult() instanceof Configuration);
        Configuration testData = (Configuration) unmarshallerResult.getResult();
        assertTrue(initializeConfiguration(testData));

        return testData;
    }

    @Test
    public void testDynamicDiscoveryClientSelection() throws Exception {
        Configuration testData = initializeConfiguration(DYNAMIC_DISCOVERY_ENABLED);
        doReturn(true).when(configurationDAO).configurationExists();
        doReturn(testData).when(configurationDAO).readEager();

        /* test default selection of dynamic discovery client OASIS compliant*/
        dynamicDiscoveryPModeProvider.init();
        assertTrue(dynamicDiscoveryPModeProvider.dynamicDiscoveryService instanceof DynamicDiscoveryServiceOASIS);

        /* test selection of dynamic discovery client Peppol compliant*/
        doReturn(DynamicDiscoveryClientSpecification.PEPPOL.getName()).when(domibusProperties).getProperty(anyString(), anyString());
        dynamicDiscoveryPModeProvider.init();
        assertTrue(dynamicDiscoveryPModeProvider.dynamicDiscoveryService instanceof DynamicDiscoveryServicePEPPOL);
    }

    @Test
    public void testFindDynamicProcesses() throws Exception {
        Configuration testData = initializeConfiguration(DYNAMIC_DISCOVERY_ENABLED);
        doReturn(true).when(configurationDAO).configurationExists();
        doReturn(testData).when(configurationDAO).readEager();
        dynamicDiscoveryPModeProvider.init();
        assertTrue(dynamicDiscoveryPModeProvider.dynamicResponderProcesses.size() == 1);
        assertTrue(dynamicDiscoveryPModeProvider.dynamicInitiatorProcesses.size() == 1);
        dynamicDiscoveryPModeProvider.refresh();
        assertTrue(dynamicDiscoveryPModeProvider.dynamicResponderProcesses.size() == 1);
        assertTrue(dynamicDiscoveryPModeProvider.dynamicInitiatorProcesses.size() == 1);
    }

    @Test
    public void testDoDynamicDiscoveryOnSender() throws Exception {
        Configuration testData = initializeConfiguration(DYNAMIC_DISCOVERY_ENABLED);
        doReturn(true).when(configurationDAO).configurationExists();
        doReturn(testData).when(configurationDAO).readEager();
        dynamicDiscoveryPModeProvider.init();

        EndpointInfo testDataEndpoint = buildAS4EndpointWithArguments(PROCESSIDENTIFIER_ID, PROCESSIDENTIFIER_SCHEME, ADDRESS, ALIAS_CN_AVAILABLE);
        doReturn(testDataEndpoint).when(dynamicDiscoveryServiceOASIS).lookupInformation(UNKNOWN_DYNAMIC_RESPONDER_PARTYID_VALUE, UNKNOWN_DYNAMIC_RESPONDER_PARTYID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        doReturn(KeyStore.getInstance(KeyStore.getDefaultType())).when(cryptoService).getTrustStore();
        doReturn(true).when(cryptoService).addCertificate(testDataEndpoint.getCertificate(), EXPECTED_COMMON_NAME, true);
        UserMessage userMessage = buildUserMessageForDoDynamicThingsWithArguments(TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE, UNKNOWN_DYNAMIC_RESPONDER_PARTYID_VALUE, UNKNOWN_DYNAMIC_RESPONDER_PARTYID_TYPE, UNKNOWN_DYNAMIC_INITIATOR_PARTYID_VALUE, UNKNOWN_DYNAMIC_INITIATOR_PARTYID_TYPE, UUID.randomUUID().toString());
        dynamicDiscoveryPModeProvider.doDynamicDiscovery(userMessage, MSHRole.SENDING);
        Party expectedParty = new Party();
        expectedParty.setName(EXPECTED_COMMON_NAME);
        expectedParty.setEndpoint(ADDRESS);
        Identifier expectedIdentifier = new Identifier();
        expectedIdentifier.setPartyId(EXPECTED_COMMON_NAME);
        PartyIdType expectedPartyIType = new PartyIdType();
        expectedPartyIType.setName(dynamicDiscoveryPModeProvider.URN_TYPE_VALUE);
        expectedPartyIType.setValue(dynamicDiscoveryPModeProvider.URN_TYPE_VALUE);
        expectedIdentifier.setPartyIdType(expectedPartyIType);
        expectedParty.getIdentifiers().add(expectedIdentifier);
        assertTrue(dynamicDiscoveryPModeProvider.getConfiguration().getBusinessProcesses().getParties().contains(expectedParty));
    }

    @Test(expected = EbMS3Exception.class)
    public void testDoDynamicDiscoveryOnSenderNullCertificate() throws Exception {
        Configuration testData = initializeConfiguration(DYNAMIC_DISCOVERY_ENABLED);
        doReturn(true).when(configurationDAO).configurationExists();
        doReturn(testData).when(configurationDAO).readEager();
        dynamicDiscoveryPModeProvider.init();

        EndpointInfo testDataEndpoint = buildAS4EndpointWithArguments(PROCESSIDENTIFIER_ID, PROCESSIDENTIFIER_SCHEME, null, ALIAS_CN_AVAILABLE);
        doReturn(testDataEndpoint).when(dynamicDiscoveryServiceOASIS).lookupInformation(UNKNOWN_DYNAMIC_RESPONDER_PARTYID_VALUE, UNKNOWN_DYNAMIC_RESPONDER_PARTYID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        doReturn(null).when(cryptoService).getTrustStore();
        doReturn(true).when(cryptoService).addCertificate(testDataEndpoint.getCertificate(), EXPECTED_COMMON_NAME, true);
        UserMessage userMessage = buildUserMessageForDoDynamicThingsWithArguments(TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE, UNKNOWN_DYNAMIC_RESPONDER_PARTYID_VALUE, UNKNOWN_DYNAMIC_RESPONDER_PARTYID_TYPE, UNKNOWN_DYNAMIC_INITIATOR_PARTYID_VALUE, UNKNOWN_DYNAMIC_INITIATOR_PARTYID_TYPE, UUID.randomUUID().toString());
        dynamicDiscoveryPModeProvider.doDynamicDiscovery(userMessage, MSHRole.SENDING);
    }

    @Test
    public void testDoDynamicDiscoveryOnReceiver() throws Exception {
        Configuration testData = initializeConfiguration(DYNAMIC_DISCOVERY_ENABLED);
        doReturn(true).when(configurationDAO).configurationExists();
        doReturn(testData).when(configurationDAO).readEager();
        dynamicDiscoveryPModeProvider.init();

        UserMessage userMessage = buildUserMessageForDoDynamicThingsWithArguments(TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE, UNKNOWN_DYNAMIC_RESPONDER_PARTYID_VALUE, UNKNOWN_DYNAMIC_RESPONDER_PARTYID_TYPE, UNKNOWN_DYNAMIC_INITIATOR_PARTYID_VALUE, UNKNOWN_DYNAMIC_INITIATOR_PARTYID_TYPE, UUID.randomUUID().toString());
        dynamicDiscoveryPModeProvider.doDynamicDiscovery(userMessage, MSHRole.RECEIVING);
        Party expectedParty = new Party();
        expectedParty.setName(UNKNOWN_DYNAMIC_INITIATOR_PARTYID_VALUE);
        expectedParty.setEndpoint("");
        Identifier expectedIdentifier = new Identifier();
        expectedIdentifier.setPartyId(UNKNOWN_DYNAMIC_INITIATOR_PARTYID_VALUE);
        PartyIdType expectedPartyIType = new PartyIdType();
        expectedPartyIType.setName(UNKNOWN_DYNAMIC_INITIATOR_PARTYID_TYPE);
        expectedPartyIType.setValue(UNKNOWN_DYNAMIC_INITIATOR_PARTYID_TYPE);
        expectedIdentifier.setPartyIdType(expectedPartyIType);
        expectedParty.getIdentifiers().add(expectedIdentifier);
        expectedParty.setEndpoint(dynamicDiscoveryPModeProvider.MSH_ENDPOINT);
        assertTrue(dynamicDiscoveryPModeProvider.getConfiguration().getBusinessProcesses().getParties().contains(expectedParty));
    }

    @Test
    public void testFindDynamicReceiverProcesses_DynResponderAndPartySelf_ProcessInResultExpected() throws Exception {
        Configuration testData = initializeConfiguration(DYNRESPONDER_AND_PARTYSELF);
        DynamicDiscoveryPModeProvider classUnderTest = mock(DynamicDiscoveryPModeProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(testData).when(classUnderTest).getConfiguration();

        Collection<Process> result = classUnderTest.findDynamicResponderProcesses();

        assertEquals(1, result.size());

        Process foundProcess = result.iterator().next();
        assertTrue(foundProcess.isDynamicResponder());
        assertEquals(EXPECTED_DYNAMIC_PROCESS_NAME, foundProcess.getName());
    }

    @Test
    public void testFindDynamicReceiverProcesses_MultipleDynResponderAndPartySelf_MultipleProcessesInResultExpected() throws Exception {
        Configuration testData = initializeConfiguration(MULTIPLE_DYNRESPONDER_AND_PARTYSELF);
        DynamicDiscoveryPModeProvider classUnderTest = mock(DynamicDiscoveryPModeProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(testData).when(classUnderTest).getConfiguration();

        Collection<Process> result = classUnderTest.findDynamicResponderProcesses();

        assertEquals(3, result.size());

        for (Process process : result) {
            assertTrue(process.isDynamicResponder());
            assertNotEquals(UNEXPECTED_DYNAMIC_PROCESS_NAME, process.getName());
        }
    }

    @Test
    public void testFindDynamicReceiverProcesses_MultipleDynInitiatorAndPartySelf_NoProcessesInResultExpected() throws Exception {
        Configuration testData = initializeConfiguration(MULTIPLE_DYNINITIATOR_AND_PARTYSELF);

        DynamicDiscoveryPModeProvider classUnderTest = mock(DynamicDiscoveryPModeProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(testData).when(classUnderTest).getConfiguration();

        Collection<Process> result = classUnderTest.findDynamicResponderProcesses();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindDynamicReceiverProcesses_MultipleDynResponderAndDynInitiator_MultipleInResultExpected() throws Exception {
        Configuration testData = initializeConfiguration(MULTIPLE_DYNRESPONDER_AND_DYNINITIATOR);

        DynamicDiscoveryPModeProvider classUnderTest = mock(DynamicDiscoveryPModeProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(testData).when(classUnderTest).getConfiguration();

        Collection<Process> result = classUnderTest.findDynamicResponderProcesses();

        assertEquals(3, result.size());

        for (Process process : result) {
            assertTrue(process.isDynamicInitiator());
            assertTrue(process.isDynamicResponder());
        }
    }

    @Test
    public void testDoDynamicThings_NoCandidates_EbMS3ExceptionExpected() throws Exception {
        thrown.expect(EbMS3Exception.class);

        Configuration testData = initializeConfiguration(NO_DYNINITIATOR_AND_NOT_SELF);

        DynamicDiscoveryPModeProvider classUnderTest = mock(DynamicDiscoveryPModeProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(testData).when(classUnderTest).getConfiguration();
        classUnderTest.dynamicResponderProcesses = classUnderTest.findDynamicResponderProcesses();

        UserMessage userMessage = buildUserMessageForDoDynamicThingsWithArguments(null, null, null, UNKNOWN_DYNAMIC_RESPONDER_PARTYID_VALUE, UNKNOWN_DYNAMIC_RESPONDER_PARTYID_TYPE, UNKNOWN_DYNAMIC_INITIATOR_PARTYID_VALUE, UNKNOWN_DYNAMIC_INITIATOR_PARTYID_TYPE, UUID.randomUUID().toString());

        classUnderTest.doDynamicDiscovery(userMessage, MSHRole.SENDING);
    }

    @Test
    public void testExtractCommonName_PublicKeyWithCommonNameAvailable_CorrectCommonNameExpected() throws Exception {

        X509Certificate testData = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, CERT_PASSWORD);
        assertNotNull(testData);

        String result = certificateService.extractCommonName(testData);

        assertEquals(EXPECTED_COMMON_NAME, result);
    }

    @Test
    public void testExtractCommonName_PublicKeyWithCommonNameNotAvailable_IllegalArgumentExceptionExpected() throws Exception {
        thrown.expect(IllegalArgumentException.class);

        X509Certificate testData = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_NOT_AVAILABLE, CERT_PASSWORD);
        assertNotNull(testData);

        certificateService.extractCommonName(testData);
    }


    /**
     * Build UserMessage for testing. Only the fields that are mandatory for the testing doDynamicThings are filled.
     *
     * @param action
     * @param serviceValue
     * @param serviceType
     * @param toPartyId
     * @param toPartyIdType
     * @param messageId
     * @return
     */
    private UserMessage buildUserMessageForDoDynamicThingsWithArguments(String action, String serviceValue, String serviceType, String toPartyId, String toPartyIdType, String fromPartyId, String fromPartyIdType, String messageId) {

        ObjectFactory ebmsObjectFactory = new ObjectFactory();

        UserMessage userMessageToBuild = ebmsObjectFactory.createUserMessage();

        MessageInfo messageInfo = ebmsObjectFactory.createMessageInfo();
        messageInfo.setMessageId(messageId);

        userMessageToBuild.setMessageInfo(messageInfo);


        Service serviceObject = ebmsObjectFactory.createService();
        serviceObject.setValue(serviceValue);
        serviceObject.setType(serviceType);

        CollaborationInfo collaborationInfo = ebmsObjectFactory.createCollaborationInfo();
        collaborationInfo.setAction(action);
        collaborationInfo.setService(serviceObject);

        userMessageToBuild.setCollaborationInfo(collaborationInfo);

        Property property = new Property();
        property.setName(MessageConstants.FINAL_RECIPIENT);
        property.setValue(toPartyId);
        property.setType((toPartyIdType));

        PartyId partyId = ebmsObjectFactory.createPartyId();
        partyId.setValue(toPartyId);
        partyId.setType((toPartyIdType));

        To to = ebmsObjectFactory.createTo();
        to.getPartyId().add(partyId);

        PartyInfo partyInfo = ebmsObjectFactory.createPartyInfo();
        partyInfo.setTo(to);

        partyId = ebmsObjectFactory.createPartyId();
        partyId.setValue(fromPartyId);
        partyId.setType((fromPartyIdType));

        From from = ebmsObjectFactory.createFrom();
        from.getPartyId().add(partyId);
        partyInfo.setFrom(from);

        userMessageToBuild.setPartyInfo(partyInfo);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getProperty().add(property);
        userMessageToBuild.setMessageProperties(messageProperties);

        return userMessageToBuild;
    }

    private EndpointInfo buildAS4EndpointWithArguments(String processIdentifierId, String processIdentifierScheme, String address, String alias) {
        ProcessIdentifier processIdentifier = new ProcessIdentifier(processIdentifierId, processIdentifierScheme);
        TransportProfile transportProfile = new TransportProfile(DynamicDiscoveryService.transportProfileAS4);
        X509Certificate x509Certificate = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, alias, CERT_PASSWORD);

        Endpoint endpoint = new Endpoint(processIdentifier, transportProfile, address, x509Certificate);

        return new EndpointInfo(endpoint.getAddress(), endpoint.getCertificate());
    }


    /**
     * Calls private method {@code Configuration#preparePersist} in order to initialize the configuration object properly
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
    private X509Certificate loadCertificateFromJKS(String filePath, String alias) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, "1234".toCharArray());

            Certificate cert = keyStore.getCertificate(alias);

            return (X509Certificate) cert;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}