package eu.domibus.common.services.impl;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.util.EndpointInfo;
import eu.domibus.common.util.ProxyUtil;
import eu.domibus.dynamicdiscovery.ApacheFetcherForTest;
import eu.domibus.pki.CertificateService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import no.difi.vefa.peppol.common.lang.EndpointNotFoundException;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.common.lang.PeppolParsingException;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.LookupClientBuilder;
import no.difi.vefa.peppol.lookup.api.LookupException;
import no.difi.vefa.peppol.lookup.locator.BusdoxLocator;
import no.difi.vefa.peppol.mode.*;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;
import no.difi.vefa.peppol.security.util.EmptyCertificateValidator;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JMockit.class)
public class DynamicDiscoveryServicePEPPOLTest {

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";

    private static final String TEST_KEYSTORE = "testkeystore.jks";

    //The (sub)domain of the SML, e.g. acc.edelivery.tech.ec.europa.eu
    //private static final String TEST_SML_ZONE = "isaitb.acc.edelivery.tech.ec.europa.eu";
    private static final String TEST_SML_ZONE = "acc.edelivery.tech.ec.europa.eu";

    private static final String ALIAS_CN_AVAILABLE = "cn_available";
    private static final String TEST_KEYSTORE_PASSWORD = "1234";

    private static final String TEST_RECEIVER_ID = "0088:unknownRecipient";
    private static final String TEST_RECEIVER_ID_TYPE = "iso6523-actorid-upis";
    private static final String TEST_ACTION_VALUE = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1";
    private static final String TEST_SERVICE_VALUE = "scheme::serviceValue";
    private static final String TEST_SERVICE_TYPE = "serviceType";
    private static final String TEST_INVALID_SERVICE_VALUE = "invalidServiceValue";
    private static final String DOMAIN = "default";

    private static final String ADDRESS = "http://localhost:9090/anonymous/msh";

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private CertificateService certificateService;

    @Injectable
    ProxyUtil proxyUtil;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Tested
    private DynamicDiscoveryServicePEPPOL dynamicDiscoveryServicePEPPOL;

    @Test
    public void testLookupInformationMock(final @Capturing LookupClient smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getDomainProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getDomainProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_MODE, (String) any);
            result = Mode.TEST;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
            result = sm;

        }};

        EndpointInfo endpoint = dynamicDiscoveryServicePEPPOL.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
        }};
    }

    @Test(expected = ConfigurationException.class)
    public void testLookupInformationNotFound(final @Capturing LookupClient smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getDomainProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getDomainProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_MODE, (String) any);
            result = Mode.TEST;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
            result = sm;

        }};

        dynamicDiscoveryServicePEPPOL.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_INVALID_SERVICE_VALUE, TEST_SERVICE_TYPE);
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

    /* This is not a unit tests but a useful test for a real SMP entry. */
    @Test
    @Ignore
    public void testLookupInformation() throws Exception {
        new NonStrictExpectations() {{
            domibusConfigurationService.useProxy();
            result = true;

            domibusPropertyProvider.getDomainProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getDomainProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_MODE, (String) any);
            result = Mode.TEST;
        }};

        EndpointInfo endpoint = dynamicDiscoveryServicePEPPOL.lookupInformation(DOMAIN, "0088:260420181111", "iso6523-actorid-upis", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-12::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0", "cenbii-procid-ubl::urn:www.cenbii.eu:profile:bii04:ver1.0", "");

        assertNotNull(endpoint);
        System.out.println(endpoint.getAddress());
    }
}