package eu.domibus.ebms3.sender;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.pki.CertificateService;
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
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.neethi.Policy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPMessage;
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
    private static final String VALID_PMODE_CONFIG_URI = "SamplePModes/domibus-configuration-valid.xml";
    private static final String COLON_SEPARATOR = ":";
    private static final String SENDER_BLUE_GW = "blue_gw";
    private static final String RECEIVER_RED_GW = "red_gw";
    private static final String NO_SEC_SERVICE = "noSecService";
    private static final String NO_SEC_ACTION = "noSecAction";
    private static final String LEG_NO_SECNO_SEC_ACTION = "pushNoSecnoSecAction";


    @Injectable
    PolicyService policyService;

    @Injectable
    TLSReader tlsReader;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    CertificateService certificateService;

    @Injectable
    Properties domibusProperties;

    @Injectable
    Configuration configuration;

    @Injectable
    javax.xml.ws.Service service;

    @Injectable
    org.apache.cxf.jaxws.DispatchImpl<SOAPMessage> dispatch;

    @Injectable
    Client client;

    @Injectable
    HTTPConduit httpConduit;

    @Tested
    MSHDispatcher mshDispatcher;

    @Test
    public void testDispatch_DoNothingSecurityPolicy(@Injectable final SOAPMessage requestSoapMessage, @Injectable final SOAPMessage responseSoapMessage) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, IOException, ParserConfigurationException, SAXException {
        System.setProperty("domibus.config.location", new File(".").getAbsolutePath() + "/src/test/resources");

        final String pModeKey = new StringBuilder(SENDER_BLUE_GW).append(COLON_SEPARATOR).append(RECEIVER_RED_GW).append(COLON_SEPARATOR).
                append(NO_SEC_SERVICE).append(COLON_SEPARATOR).append(NO_SEC_ACTION).append(COLON_SEPARATOR).append("OAE").append(COLON_SEPARATOR).
                append(LEG_NO_SECNO_SEC_ACTION).toString();
        //"blue_gw:red_gw:noSecService:noSecAction:OAE:pushNoSecnoSecAction";

        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        final LegConfiguration legConfiguration = getLegFromConfiguration(configuration, LEG_NO_SECNO_SEC_ACTION);
        final PolicyBuilder pb = BusFactory.getDefaultBus().getExtension(PolicyBuilder.class);
        final Policy doNothingPolicy = pb.getPolicy(new FileInputStream(new File("./src/test/resources", "policies/doNothingPolicy.xml")));
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

            dispatch.invoke(withAny(requestSoapMessage));
            result = responseSoapMessage;
        }};

        mshDispatcher.dispatch(requestSoapMessage, pModeKey);

        new Verifications() {{
            dispatch.invoke(requestSoapMessage);
        }};

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
