package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.DomibusCertificateException;
import eu.domibus.pki.PolicyService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Arun Raj
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MSHDispatcherTest {

    private static final Log LOG = LogFactory.getLog(MSHDispatcherTest.class);
    private static final String TEST_RESOURCES_DIR = "./src/test/resources";
    private static final String VALID_PMODE_CONFIG_URI = "SamplePModes/domibus-configuration-valid.xml";
    private static final String COLON_SEPARATOR = ":";
    private static final String SENDER_BLUE_GW = "blue_gw";
    private static final String RECEIVER_RED_GW = "red_gw";
    private static final String NO_SEC_SERVICE = "noSecService";
    private static final String NO_SEC_ACTION = "noSecAction";
    private static final String LEG_NO_SECNO_SEC_ACTION = "pushNoSecnoSecAction";
    private static final String TEST_SERVICE1 = "testService1";
    private static final String TC1ACTION = "tc1Action";
    private static final String OAE = "OAE";
    private static final String PUSH_TESTCASE1_TC1ACTION = "pushTestcase1tc1Action";

    @Injectable
    PolicyService policyService;

    @Injectable
    TLSReader tlsReader;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    CertificateService certificateService;

    @Injectable
    LegConfiguration legConfiguration;

    @Injectable
    Properties domibusProperties;

    @Injectable
    Configuration configuration;

    @Injectable
    javax.xml.ws.Service service;

    @Injectable
    org.apache.cxf.jaxws.DispatchImpl<SOAPMessage> dispatch;

    @Injectable
    HTTPClientPolicy httpClientPolicy;

    @Injectable
    Client client;

    @Injectable
    HTTPConduit httpConduit;

    @Injectable
    TLSClientParameters tlsClientParameters;

    @Tested
    MSHDispatcher mshDispatcher;


    /**
     * Happy flow testing with actual data
     *
     * @param requestSoapMessage
     * @param responseSoapMessage
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws JAXBException
     * @throws EbMS3Exception
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test
    public void testDispatch_DoNothingSecurityPolicy_HappyFlow(@Injectable final SOAPMessage requestSoapMessage, @Injectable final SOAPMessage responseSoapMessage) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, IOException, ParserConfigurationException, SAXException {
        System.setProperty("domibus.config.location", TEST_RESOURCES_DIR);

        //"blue_gw:red_gw:noSecService:noSecAction:OAE:pushNoSecnoSecAction";
        final String pModeKey = new StringBuilder(SENDER_BLUE_GW).append(COLON_SEPARATOR).append(RECEIVER_RED_GW).append(COLON_SEPARATOR).
                append(NO_SEC_SERVICE).append(COLON_SEPARATOR).append(NO_SEC_ACTION).append(COLON_SEPARATOR).append(OAE).append(COLON_SEPARATOR).
                append(LEG_NO_SECNO_SEC_ACTION).toString();


        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        legConfiguration = getLegFromConfiguration(configuration, LEG_NO_SECNO_SEC_ACTION);
        final PolicyBuilder pb = BusFactory.getDefaultBus().getExtension(PolicyBuilder.class);
        final Policy doNothingPolicy = pb.getPolicy(new FileInputStream(new File(TEST_RESOURCES_DIR, "policies/doNothingPolicy.xml")));
        final String endPoint = getPartyFromConfiguration(configuration, RECEIVER_RED_GW).getEndpoint();
        final Map requestContextMap = new HashMap();

        new Expectations(mshDispatcher) {{

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            pModeProvider.getSenderParty(pModeKey);
            result = getPartyFromConfiguration(configuration, SENDER_BLUE_GW);

            pModeProvider.getReceiverParty(pModeKey);
            result = getPartyFromConfiguration(configuration, RECEIVER_RED_GW);

            policyService.parsePolicy(withSubstring("doNothingPolicy"));
            result = doNothingPolicy;

            policyService.isNoSecurityPolicy(doNothingPolicy);
            result = true;

            mshDispatcher.createWSServiceDispatcher(endPoint);
            result = dispatch;

            dispatch.getRequestContext();
            result = requestContextMap;

            dispatch.getClient();
            result = client;

            client.getConduit();
            result = httpConduit;

            domibusProperties.getProperty(withSubstring("connectionTimeout"), "120000");
            result = "120";

            domibusProperties.getProperty(withSubstring("receiveTimeout"), "120000");
            result = "120";

            domibusProperties.getProperty("domibus.proxy.enabled", "false");
            result = "false";

        }};

        mshDispatcher.dispatch(requestSoapMessage, pModeKey);

        new Verifications() {{
            certificateService.isCertificateValid(anyString);
            times = 0;
            certificateService.isCertificateValidationEnabled();
            times = 0;
            certificateService.isCertificateChainValid(anyString);
            times = 0;
            tlsReader.getTlsClientParameters();
            mshDispatcher.configureProxy(withAny(httpClientPolicy), withAny(httpConduit));
            times = 0;
            dispatch.invoke(requestSoapMessage);
        }};

    }

    /**
     * Testing with actual data
     * @param requestSoapMessage
     * @param responseSoapMessage
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws JAXBException
     * @throws EbMS3Exception
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test
    public void testDispatch_tc1Process_HappyFlow(@Injectable final SOAPMessage requestSoapMessage, @Injectable final SOAPMessage responseSoapMessage) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, IOException, ParserConfigurationException, SAXException {
        System.setProperty("domibus.config.location", TEST_RESOURCES_DIR);

        //"blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        final String pModeKey = new StringBuilder(SENDER_BLUE_GW).append(COLON_SEPARATOR).append(RECEIVER_RED_GW).append(COLON_SEPARATOR).
                append(TEST_SERVICE1).append(COLON_SEPARATOR).append(TC1ACTION).append(COLON_SEPARATOR).append(OAE).append(COLON_SEPARATOR).
                append(PUSH_TESTCASE1_TC1ACTION).toString();


        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        legConfiguration = getLegFromConfiguration(configuration, PUSH_TESTCASE1_TC1ACTION);
        final PolicyBuilder pb = BusFactory.getDefaultBus().getExtension(PolicyBuilder.class);
        final Policy signOnlyPolicy = pb.getPolicy(new FileInputStream(new File(TEST_RESOURCES_DIR, "policies/signOnly.xml")));
        //replace receiver end point as https: to enable setting TLS client params.
        final Party receiverParty = getPartyFromConfiguration(configuration, RECEIVER_RED_GW);
        final String endPoint = receiverParty.getEndpoint().replace("http:", "https:");
        receiverParty.setEndpoint(endPoint);
        final Map requestContextMap = new HashMap();

        new Expectations(mshDispatcher) {{
            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            pModeProvider.getSenderParty(pModeKey);
            result = getPartyFromConfiguration(configuration, SENDER_BLUE_GW);

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            policyService.parsePolicy(withSubstring("signOnly"));
            result = signOnlyPolicy;

            policyService.isNoSecurityPolicy(signOnlyPolicy);
            result = false;

            certificateService.isCertificateValid(anyString);
            result = true;

            certificateService.isCertificateValidationEnabled();
            result = true;

            certificateService.isCertificateChainValid(anyString);
            result = false;

            mshDispatcher.createWSServiceDispatcher(endPoint);
            result = dispatch;

            dispatch.getRequestContext();
            result = requestContextMap;

            dispatch.getClient();
            result = client;

            client.getConduit();
            result = httpConduit;

            domibusProperties.getProperty(withSubstring("connectionTimeout"), "120000");
            result = "120";

            domibusProperties.getProperty(withSubstring("receiveTimeout"), "120000");
            result = "120";

            tlsReader.getTlsClientParameters();
            result = tlsClientParameters;

            domibusProperties.getProperty("domibus.proxy.enabled", "false");
            result = "true";

            domibusProperties.getProperty("domibus.proxy.http.host");
            result = "1.2.3.4";

            domibusProperties.getProperty("domibus.proxy.http.port");
            result = "1234";

            domibusProperties.getProperty("domibus.proxy.user");
            result = "test";

            domibusProperties.getProperty("domibus.proxy.password");
            result = "test";

            domibusProperties.getProperty("domibus.proxy.nonProxyHosts");
            result = "5.6.7.8";
        }};

        mshDispatcher.dispatch(requestSoapMessage, pModeKey);

        new Verifications() {{
            certificateService.isCertificateValid(anyString);
            certificateService.isCertificateValidationEnabled();
            certificateService.isCertificateChainValid(anyString);
            tlsReader.getTlsClientParameters();
            mshDispatcher.configureProxy(withAny(httpClientPolicy), withAny(httpConduit));
            dispatch.invoke(requestSoapMessage);
        }};
    }

    @Test
    public void testDispatch_SecurityPolicyException(@Injectable final SOAPMessage requestSoapMessage, @Injectable final SOAPMessage responseSoapMessage) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, IOException, ParserConfigurationException, SAXException {
        System.setProperty("domibus.config.location", TEST_RESOURCES_DIR);

        //"blue_gw:red_gw:noSecService:noSecAction:OAE:pushNoSecnoSecAction";
        final String pModeKey = new StringBuilder(SENDER_BLUE_GW).append(COLON_SEPARATOR).append(RECEIVER_RED_GW).append(COLON_SEPARATOR).
                append(NO_SEC_SERVICE).append(COLON_SEPARATOR).append(NO_SEC_ACTION).append(COLON_SEPARATOR).append(OAE).append(COLON_SEPARATOR).
                append(LEG_NO_SECNO_SEC_ACTION).toString();


        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        legConfiguration = getLegFromConfiguration(configuration, LEG_NO_SECNO_SEC_ACTION);

        new Expectations(mshDispatcher) {{

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            pModeProvider.getSenderParty(pModeKey);
            result = getPartyFromConfiguration(configuration, SENDER_BLUE_GW);

            pModeProvider.getReceiverParty(pModeKey);
            result = getPartyFromConfiguration(configuration, RECEIVER_RED_GW);

            policyService.parsePolicy(withSubstring("doNothingPolicy"));
            result = new ConfigurationException();
        }};

        try {
            mshDispatcher.dispatch(requestSoapMessage, pModeKey);
            Assert.fail("Expected no policy exception");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof eu.domibus.common.exception.EbMS3Exception);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0010, ((EbMS3Exception) e).getErrorCode());
        }

        new Verifications() {{
            certificateService.isCertificateValid(anyString);
            times = 0;
            certificateService.isCertificateValidationEnabled();
            times = 0;
            certificateService.isCertificateChainValid(anyString);
            times = 0;
            tlsReader.getTlsClientParameters();
            times = 0;
            mshDispatcher.configureProxy(withAny(httpClientPolicy), withAny(httpConduit));
            times = 0;
            dispatch.invoke(requestSoapMessage);
            times = 0;
        }};
    }

    @Test
    public void testDispatch_SenderCertificateException(@Injectable final SOAPMessage requestSoapMessage, @Injectable final SOAPMessage responseSoapMessage) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, IOException, ParserConfigurationException, SAXException {
        System.setProperty("domibus.config.location", TEST_RESOURCES_DIR);

        //"blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        final String pModeKey = new StringBuilder(SENDER_BLUE_GW).append(COLON_SEPARATOR).append(RECEIVER_RED_GW).append(COLON_SEPARATOR).
                append(TEST_SERVICE1).append(COLON_SEPARATOR).append(TC1ACTION).append(COLON_SEPARATOR).append(OAE).append(COLON_SEPARATOR).
                append(PUSH_TESTCASE1_TC1ACTION).toString();


        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        legConfiguration = getLegFromConfiguration(configuration, PUSH_TESTCASE1_TC1ACTION);
        final PolicyBuilder pb = BusFactory.getDefaultBus().getExtension(PolicyBuilder.class);
        final Policy signOnlyPolicy = pb.getPolicy(new FileInputStream(new File(TEST_RESOURCES_DIR, "policies/signOnly.xml")));
        //replace receiver end point as https: to enable setting TLS client params.

        new Expectations(mshDispatcher) {{
            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            pModeProvider.getSenderParty(pModeKey);
            result = getPartyFromConfiguration(configuration, SENDER_BLUE_GW);

            pModeProvider.getReceiverParty(pModeKey);
            result = getPartyFromConfiguration(configuration, RECEIVER_RED_GW);

            policyService.parsePolicy(withSubstring("signOnly"));
            result = signOnlyPolicy;

            policyService.isNoSecurityPolicy(signOnlyPolicy);
            result = false;

            certificateService.isCertificateValid(anyString);
            result = new DomibusCertificateException();

        }};

        try {
            mshDispatcher.dispatch(requestSoapMessage, pModeKey);
            Assert.fail("Expected Domibus Certificate exception to be raised!");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof eu.domibus.common.exception.EbMS3Exception);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0101, ((EbMS3Exception) e).getErrorCode());
        }

        new Verifications() {{
            mshDispatcher.configureProxy(withAny(httpClientPolicy), withAny(httpConduit));
            times = 0;
            dispatch.invoke(requestSoapMessage);
            times = 0;
        }};
    }

    /**
     * Even if receiver certificate chain validation raises exception, message sending should proceed.
     *
     * @param requestSoapMessage
     * @param responseSoapMessage
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws JAXBException
     * @throws EbMS3Exception
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test
    public void testDispatch_ReceiverCertificateException(@Injectable final SOAPMessage requestSoapMessage, @Injectable final SOAPMessage responseSoapMessage) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, IOException, ParserConfigurationException, SAXException {
        System.setProperty("domibus.config.location", TEST_RESOURCES_DIR);

        //"blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        final String pModeKey = new StringBuilder(SENDER_BLUE_GW).append(COLON_SEPARATOR).append(RECEIVER_RED_GW).append(COLON_SEPARATOR).
                append(TEST_SERVICE1).append(COLON_SEPARATOR).append(TC1ACTION).append(COLON_SEPARATOR).append(OAE).append(COLON_SEPARATOR).
                append(PUSH_TESTCASE1_TC1ACTION).toString();


        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        legConfiguration = getLegFromConfiguration(configuration, PUSH_TESTCASE1_TC1ACTION);
        final PolicyBuilder pb = BusFactory.getDefaultBus().getExtension(PolicyBuilder.class);
        final Policy signOnlyPolicy = pb.getPolicy(new FileInputStream(new File(TEST_RESOURCES_DIR, "policies/signOnly.xml")));
        //replace receiver end point as https: to enable setting TLS client params.
        final Party receiverParty = getPartyFromConfiguration(configuration, RECEIVER_RED_GW);
        final String endPoint = receiverParty.getEndpoint().replace("http:", "https:");
        receiverParty.setEndpoint(endPoint);
        final Map requestContextMap = new HashMap();

        new Expectations(mshDispatcher) {{
            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            pModeProvider.getSenderParty(pModeKey);
            result = getPartyFromConfiguration(configuration, SENDER_BLUE_GW);

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            policyService.parsePolicy(withSubstring("signOnly"));
            result = signOnlyPolicy;

            policyService.isNoSecurityPolicy(signOnlyPolicy);
            result = false;

            certificateService.isCertificateValid(anyString);
            result = true;

            certificateService.isCertificateValidationEnabled();
            result = true;

            certificateService.isCertificateChainValid(anyString);
            result = new Exception();

            mshDispatcher.createWSServiceDispatcher(endPoint);
            result = dispatch;

            dispatch.getRequestContext();
            result = requestContextMap;

            dispatch.getClient();
            result = client;

            client.getConduit();
            result = httpConduit;

            domibusProperties.getProperty(withSubstring("connectionTimeout"), "120000");
            result = "120";

            domibusProperties.getProperty(withSubstring("receiveTimeout"), "120000");
            result = "120";

            tlsReader.getTlsClientParameters();
            result = tlsClientParameters;

            domibusProperties.getProperty("domibus.proxy.enabled", "false");
            result = "true";

            domibusProperties.getProperty("domibus.proxy.http.host");
            result = "1.2.3.4";

            domibusProperties.getProperty("domibus.proxy.http.port");
            result = "1234";

            domibusProperties.getProperty("domibus.proxy.user");
            result = "test";

            domibusProperties.getProperty("domibus.proxy.password");
            result = "test";

            domibusProperties.getProperty("domibus.proxy.nonProxyHosts");
            result = "5.6.7.8";
        }};

        mshDispatcher.dispatch(requestSoapMessage, pModeKey);

        new Verifications() {{
            tlsReader.getTlsClientParameters();
            mshDispatcher.configureProxy(withAny(httpClientPolicy), withAny(httpConduit));
            dispatch.invoke(requestSoapMessage);
        }};
    }

    @Test
    public void testDispatch_ExceptionDuringDispatch(@Injectable final SOAPMessage requestSoapMessage, @Injectable final SOAPMessage responseSoapMessage) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, IOException, ParserConfigurationException, SAXException {
        System.setProperty("domibus.config.location", TEST_RESOURCES_DIR);

        //"blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        final String pModeKey = new StringBuilder(SENDER_BLUE_GW).append(COLON_SEPARATOR).append(RECEIVER_RED_GW).append(COLON_SEPARATOR).
                append(TEST_SERVICE1).append(COLON_SEPARATOR).append(TC1ACTION).append(COLON_SEPARATOR).append(OAE).append(COLON_SEPARATOR).
                append(PUSH_TESTCASE1_TC1ACTION).toString();


        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        legConfiguration = getLegFromConfiguration(configuration, PUSH_TESTCASE1_TC1ACTION);
        final PolicyBuilder pb = BusFactory.getDefaultBus().getExtension(PolicyBuilder.class);
        final Policy signOnlyPolicy = pb.getPolicy(new FileInputStream(new File(TEST_RESOURCES_DIR, "policies/signOnly.xml")));
        //replace receiver end point as https: to enable setting TLS client params.
        final Party receiverParty = getPartyFromConfiguration(configuration, RECEIVER_RED_GW);
        final String endPoint = receiverParty.getEndpoint().replace("http:", "https:");
        receiverParty.setEndpoint(endPoint);
        final Map requestContextMap = new HashMap();

        new Expectations(mshDispatcher) {{
            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            pModeProvider.getSenderParty(pModeKey);
            result = getPartyFromConfiguration(configuration, SENDER_BLUE_GW);

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            policyService.parsePolicy(withSubstring("signOnly"));
            result = signOnlyPolicy;

            policyService.isNoSecurityPolicy(signOnlyPolicy);
            result = false;

            certificateService.isCertificateValid(anyString);
            result = true;

            certificateService.isCertificateValidationEnabled();
            result = true;

            certificateService.isCertificateChainValid(anyString);
            result = false;

            mshDispatcher.createWSServiceDispatcher(endPoint);
            result = dispatch;

            dispatch.getRequestContext();
            result = requestContextMap;

            dispatch.getClient();
            result = client;

            client.getConduit();
            result = httpConduit;

            domibusProperties.getProperty(withSubstring("connectionTimeout"), "120000");
            result = "120";

            domibusProperties.getProperty(withSubstring("receiveTimeout"), "120000");
            result = "120";

            tlsReader.getTlsClientParameters();
            result = tlsClientParameters;

            domibusProperties.getProperty("domibus.proxy.enabled", "false");
            result = "true";

            domibusProperties.getProperty("domibus.proxy.http.host");
            result = "1.2.3.4";

            domibusProperties.getProperty("domibus.proxy.http.port");
            result = "1234";

            domibusProperties.getProperty("domibus.proxy.user");
            result = "test";

            domibusProperties.getProperty("domibus.proxy.password");
            result = "test";

            domibusProperties.getProperty("domibus.proxy.nonProxyHosts");
            result = "5.6.7.8";

            dispatch.invoke(requestSoapMessage);
            result = new WebServiceException();
        }};

        try {
            mshDispatcher.dispatch(requestSoapMessage, pModeKey);
            Assert.fail("Webservice Exception was expected");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof EbMS3Exception);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0005, ((EbMS3Exception) e).getErrorCode());
        }

    }

    @Test
    public void testCreateWSServiceDispatcher() {

        try {
            mshDispatcher.createWSServiceDispatcher("TestEndPoint");
        } catch (Exception e) {
            Assert.fail("No exception was expected!");
        }
    }

    private Party getPartyFromConfiguration(Configuration configuration, String partyName) {
        Party result = null;
        for (Party party : configuration.getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(partyName, party.getName())) {
                result = party;
            }
        }
        return result;
    }

    private LegConfiguration getLegFromConfiguration(Configuration configuration, String legName) {
        LegConfiguration result = null;
        for (LegConfiguration legConfiguration1 : configuration.getBusinessProcesses().getLegConfigurations()) {
            if (StringUtils.equalsIgnoreCase(legName, legConfiguration1.getName())) {
                result = legConfiguration1;
            }
        }
        return result;
    }

    public Configuration loadSamplePModeConfiguration(String samplePModeFileRelativeURI) throws JAXBException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LOG.debug("Inside sample PMode configuration");
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(samplePModeFileRelativeURI);
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        configuration = (Configuration) unmarshaller.unmarshal(xmlStream);
        Method m = configuration.getClass().getDeclaredMethod("preparePersist", null);
        m.setAccessible(true);
        m.invoke(configuration);

        return configuration;
    }

}
