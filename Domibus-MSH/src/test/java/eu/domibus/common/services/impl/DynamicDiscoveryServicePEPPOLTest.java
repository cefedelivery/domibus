package eu.domibus.common.services.impl;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.util.DomibusApacheFetcher;
import eu.domibus.common.util.EndpointInfo;
import eu.domibus.api.util.HttpUtil;
import eu.domibus.pki.CertificateService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import no.difi.vefa.peppol.common.lang.PeppolParsingException;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.LookupClientBuilder;
import no.difi.vefa.peppol.lookup.api.MetadataFetcher;
import no.difi.vefa.peppol.lookup.fetcher.AbstractFetcher;
import no.difi.vefa.peppol.mode.*;
import no.difi.vefa.peppol.lookup.LookupClient;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JMockit.class)
public class DynamicDiscoveryServicePEPPOLTest {

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";

    private static final String TEST_KEYSTORE = "testkeystore.jks";

    //The (sub)domain of the SML, e.g. acc.edelivery.tech.ec.europa.eu
    private static final String TEST_SML_ZONE = "isaitb.acc.edelivery.tech.ec.europa.eu";

    private static final String ALIAS_CN_AVAILABLE = "cn_available";
    private static final String TEST_KEYSTORE_PASSWORD = "1234";

    private static final String TEST_RECEIVER_ID = "0088:unknownRecipient";
    private static final String TEST_RECEIVER_ID_TYPE = "iso6523-actorid-upis";
    private static final String TEST_ACTION_VALUE = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1";
    private static final String TEST_SERVICE_VALUE = "scheme::serviceValue";
    private static final String TEST_SERVICE_TYPE = "serviceType";
    private static final String TEST_INVALID_SERVICE_VALUE = "invalidServiceValue";

    private static final String ADDRESS = "http://localhost:9090/anonymous/msh";

    @Injectable
    private Properties domibusProperties;

    @Injectable
    private HttpUtil httpUtil;

    @Injectable
    private CertificateService certificateService;

    @Tested
    private DynamicDiscoveryServicePEPPOL dynamicDiscoveryServicePEPPOL;

    @Test
    public void testLookupInformationMock(final @Capturing LookupClient smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusProperties.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusProperties.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_MODE, (String) any);
            result = Mode.TEST;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
            result = sm;

        }};

        EndpointInfo endpoint = dynamicDiscoveryServicePEPPOL.lookupInformation(TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
        }};
    }

    @Test(expected = ConfigurationException.class)
    public void testLookupInformationNotFound(final @Capturing LookupClient smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusProperties.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusProperties.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_MODE, (String) any);
            result = Mode.TEST;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
            result = sm;

        }};

        dynamicDiscoveryServicePEPPOL.lookupInformation(TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_INVALID_SERVICE_VALUE, TEST_SERVICE_TYPE);
    }


    private ServiceMetadata buildServiceMetadata() {

        X509Certificate testData = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        ProcessIdentifier processIdentifier;
        try {
            processIdentifier = ProcessIdentifier.parse(TEST_SERVICE_VALUE);
        } catch (PeppolParsingException e) {
            return null;
        }

        Endpoint endpoint = Endpoint.of(TransportProfile.AS4, URI.create(ADDRESS), testData);

        List<ProcessMetadata<Endpoint>> processes = new ArrayList<>();
        ProcessMetadata<Endpoint> process = ProcessMetadata.of(processIdentifier, endpoint);
        processes.add(process);

        ServiceMetadata sm = ServiceMetadata.of(null, null, processes);

        return sm;
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8280);

    /* This is not a unit tests but a useful test for a real SMP entry. */
    @Test
    //@Ignore
    public void testLookupInformation() throws Exception {
        new NonStrictExpectations() {{
            httpUtil.useProxy();
            result = false; // SET THIS VALUE TO TRUE

            httpUtil.getConfiguredCredentialsProvider();
            result = getConfiguredCredentialsProvider();

            httpUtil.getConfiguredProxy();
            result = getConfiguredProxy();

            domibusProperties.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusProperties.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_MODE, (String) any);
            result = Mode.TEST;
        }};

//        try {
//            EndpointInfo endpoint = dynamicDiscoveryServicePEPPOL.lookupInformation("1111", "222", "3333", "4444::5555", "");
            EndpointInfo endpoint = dynamicDiscoveryServicePEPPOL.lookupInformation("0088:260420181111", "iso6523-actorid-upis", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-12::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0", "cenbii-procid-ubl::urn:www.cenbii.eu:profile:bii04:ver1.0", "");
//        } catch (ConfigurationException exc) {}

        assertNotNull(endpoint);
        System.out.println(endpoint.getAddress());

//        verify(getRequestedFor(urlMatching(".*"))
//                .withRequestBody(matching(".*")));
    }

    // PUT YOUR VALUES HERE
    private static String HOST = "somehost";
    private static String PORT = "8280";
    private static String USER = "idragusa";
    private static String PASSWORD = "changeme";

    private HttpHost getConfiguredProxy() {
        String httpProxyHost = HOST;
        String httpProxyPort = PORT;
        return new HttpHost(httpProxyHost, Integer.parseInt(httpProxyPort));
    }

    private CredentialsProvider getConfiguredCredentialsProvider() {
        String httpProxyHost = HOST;
        String httpProxyPort = PORT;
        String httpProxyUser = USER;
        String httpProxyPassword = PASSWORD;

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(httpProxyHost, Integer.parseInt(httpProxyPort)),
                new UsernamePasswordCredentials(httpProxyUser, httpProxyPassword));

        return credsProvider;
    }
}