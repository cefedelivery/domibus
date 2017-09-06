package eu.domibus.ebms3.sender;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.PolicyService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang.StringUtils;
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
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHDispatcherTest.class);

    private static final String TEST_RESOURCES_DIR = "./src/test/resources";
    private static final String VALID_PMODE_CONFIG_URI = "samplePModes/domibus-configuration-valid.xml";
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
    RawEnvelopeLogDao rawEnvelopeLogDao;

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

    @Injectable
    DispatchClientProvider dispatchClientProvider;

    @Tested
    MSHDispatcher mshDispatcher;


    /**
     * Happy flow testing with actual data
     *
     * @param requestSoapMessage
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
    public void testDispatch_DoNothingSecurityPolicy_HappyFlow(@Injectable final SOAPMessage requestSoapMessage,
                                                               @Injectable final Policy policy,
                                                               @Injectable final Dispatch<SOAPMessage> dispatch) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, IOException, ParserConfigurationException, SAXException {
        final String endPoint = "http://localhost";
        final String algorithm = "algorithm";
        final String pModeKey = "myPmodeKey";
        final boolean cacheable = true;

        new Expectations(mshDispatcher) {{
            mshDispatcher.isDispatchClientCacheActivated();
            result = cacheable;

            legConfiguration.getSecurity().getSignatureMethod().getAlgorithm();
            result = algorithm;

            dispatchClientProvider.getClient(endPoint, algorithm, policy, pModeKey, cacheable);
            result = dispatch;
        }};

        mshDispatcher.dispatch(requestSoapMessage, endPoint, policy, legConfiguration, pModeKey);

        new Verifications() {{
            dispatch.invoke(requestSoapMessage);
        }};

    }

    /**
     * Even if receiver certificate chain validation raises exception, message sending should proceed.
     *
     * @param requestSoapMessage
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
    public void testDispatch_ExceptionDuringDispatch(@Injectable final SOAPMessage requestSoapMessage,
                                                     @Injectable final Policy policy,
                                                     @Injectable final Dispatch<SOAPMessage> dispatch) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, IOException, ParserConfigurationException, SAXException {
        final String endPoint = "http://localhost";
        final String algorithm = "algorithm";
        final String pModeKey = "myPmodeKey";
        final boolean cacheable = false;

        new Expectations(mshDispatcher) {{
            dispatchClientProvider.getClient(endPoint, algorithm, policy, pModeKey, cacheable);
            result = dispatch;

            legConfiguration.getSecurity().getSignatureMethod().getAlgorithm();
            result = algorithm;

            dispatch.invoke(requestSoapMessage);
            result = new WebServiceException();
        }};

        try {
            mshDispatcher.dispatch(requestSoapMessage, endPoint, policy, legConfiguration, pModeKey);
            Assert.fail("Webservice Exception was expected");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof EbMS3Exception);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0005, ((EbMS3Exception) e).getErrorCode());
        }

    }
}
