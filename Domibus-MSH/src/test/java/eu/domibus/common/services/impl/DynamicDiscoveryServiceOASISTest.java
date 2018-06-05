package eu.domibus.common.services.impl;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.HttpUtil;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.util.EndpointInfo;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.core.fetcher.FetcherResponse;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ServiceMetadata;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceGroupType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceMetadataType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.SignedServiceMetadataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.security.KeyStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 3.2.5
 */
@RunWith(JMockit.class)
public class DynamicDiscoveryServiceOASISTest {

    //The (sub)domain of the SML, e.g. ehealth.acc.edelivery.tech.ec.europa.eu, connectivitytest.acc.edelivery.tech.ec.europa.eu
    private static final String TEST_SML_ZONE = "acc.edelivery.tech.ec.europa.eu";

    private static final String TEST_KEYSTORE_PASSWORD = "test123";

    private static final String TEST_RECEIVER_ID = "urn:romania:ncpb";
    private static final String TEST_RECEIVER_ID_TYPE = "ehealth-actorid-qns";
    private static final String TEST_ACTION_VALUE = "ehealth-resid-qns:urn::epsos##services:extended:epsos::107";
    private static final String TEST_SERVICE_VALUE = "urn:epsosPatientService::List";
    private static final String TEST_SERVICE_TYPE = "ehealth-procid-qns";
    private static final String TEST_INVALID_SERVICE_VALUE = "invalidServiceValue";

    private static final String ADDRESS = "http://localhost:9090/anonymous/msh";

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private MultiDomainCryptoService multiDomainCertificateProvider;

    @Injectable
    protected DomainContextProvider domainProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Tested
    DynamicDiscoveryServiceOASIS dynamicDiscoveryServiceOASIS;

    @Test
    public void testLookupInformationMock(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
            result = sm;

        }};

        EndpointInfo endpoint = dynamicDiscoveryServiceOASIS.lookupInformation(TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
        }};
    }

    @Test
    public void testLookupInformationRegexMatch(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = "^.*EHEALTH_SMP.*$";

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
            result = sm;

        }};

        EndpointInfo endpoint = dynamicDiscoveryServiceOASIS.lookupInformation(TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
        }};
    }

    @Test(expected = ConfigurationException.class)
    public void testLookupInformationNotFound(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
            result = sm;
        }};

        dynamicDiscoveryServiceOASIS.lookupInformation(TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_INVALID_SERVICE_VALUE, TEST_SERVICE_TYPE);
    }

    @Test
    public void testLookupInformationNotFoundMessage(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
            result = sm;
        }};
        try {

            dynamicDiscoveryServiceOASIS.lookupInformation(TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_INVALID_SERVICE_VALUE, TEST_SERVICE_TYPE);
        } catch (ConfigurationException cfe) {
            Assert.assertTrue(cfe.getMessage().contains("Could not fetch metadata for: urn:romania:ncpb"));
        }
    }

    private ServiceMetadata buildServiceMetadata() throws Exception {

        InputStream inputStream = getClass().getResourceAsStream("../SignedServiceMetadataResponseOASIS.xml");
        FetcherResponse fetcherResponse = new FetcherResponse(inputStream);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        JAXBContext jaxbContext = JAXBContext.newInstance(ServiceMetadataType.class, SignedServiceMetadataType.class, ServiceGroupType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();


        Document document = documentBuilderFactory.newDocumentBuilder().parse(fetcherResponse.getInputStream());
        Object result = ((JAXBElement) unmarshaller.unmarshal(document)).getValue();
        SignedServiceMetadataType signedServiceMetadataType = (SignedServiceMetadataType) result;
        ServiceMetadata serviceMetadata = new ServiceMetadata(signedServiceMetadataType, null, "");
        return serviceMetadata;
    }

   @Test
    public void testProxyConfigured() throws Exception {
        // Given
        new Expectations(dynamicDiscoveryServiceOASIS) {{
            domibusConfigurationService.useProxy();
            result = true;

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            result = "192.168.0.0";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
            result = "1234";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
            result = "proxyUser";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);
            result = "proxyPassword";
        }};

        //when
        DefaultProxy defaultProxy = dynamicDiscoveryServiceOASIS.getConfiguredProxy();

        //then
        Assert.assertNotNull(defaultProxy);
    }

    @Test
    public void testProxyNotConfigured() throws Exception {
        // Given
        new Expectations(dynamicDiscoveryServiceOASIS) {{
            domibusConfigurationService.useProxy();
            result = false;
        }};

        //when
        DefaultProxy defaultProxy = dynamicDiscoveryServiceOASIS.getConfiguredProxy();

        //then
        Assert.assertNull(defaultProxy);
    }
    @Test
    public void testCreateDynamicDiscoveryClientWithProxy() throws Exception {
        // Given

        new Expectations(dynamicDiscoveryServiceOASIS) {{
            domibusConfigurationService.useProxy();
            result = true;

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            result = "192.168.0.1";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
            result = "8012";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
            result = "proxyUser";

            domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);
            result = "proxyPassword";

            domibusPropertyProvider.getProperty(dynamicDiscoveryServiceOASIS.SMLZONE_KEY);
            result = "domibus.domain.ec.europa.eu";

            domibusPropertyProvider.getProperty(dynamicDiscoveryServiceOASIS.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = "^.*$";
        }};

        //when
        DynamicDiscovery dynamicDiscovery = dynamicDiscoveryServiceOASIS.createDynamicDiscoveryClient();
        Assert.assertNotNull(dynamicDiscovery);
        DefaultProxy defaultProxy = (DefaultProxy) ReflectionTestUtils.getField(dynamicDiscovery.getService().getMetadataFetcher(), "proxyConfiguration");
        Assert.assertNotNull(defaultProxy);
    }

    @Test
    public void testCreateDynamicDiscoveryClientWithoutProxy() throws Exception {
        // Given

        new Expectations(dynamicDiscoveryServiceOASIS) {{
            domibusConfigurationService.useProxy();
            result = false;

            domibusPropertyProvider.getProperty(dynamicDiscoveryServiceOASIS.SMLZONE_KEY);
            result = "domibus.domain.ec.europa.eu";

            domibusPropertyProvider.getProperty(dynamicDiscoveryServiceOASIS.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = "^.*$";
        }};

        //when
        DynamicDiscovery dynamicDiscovery = dynamicDiscoveryServiceOASIS.createDynamicDiscoveryClient();
        Assert.assertNotNull(dynamicDiscovery);
        DefaultProxy defaultProxy = (DefaultProxy) ReflectionTestUtils.getField(dynamicDiscovery.getService().getMetadataFetcher(), "proxyConfiguration");
        Assert.assertNull(defaultProxy);
    }

    private void assertNullForMissingParameters() throws ConnectionException {
        //when
        DefaultProxy defaultProxy = dynamicDiscoveryServiceOASIS.getConfiguredProxy();

        //then
        Assert.assertNull(defaultProxy);
    }


    /*
    * This is not a unit tests but the code is useful to test real SMP entries.
     */
    @Test
    @Ignore
    public void testLookupInformation() throws Exception {
        new NonStrictExpectations() {{
            domibusConfigurationService.useProxy();
            result = false;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            KeyStore truststore;
            truststore = KeyStore.getInstance("JKS");
            truststore.load(getClass().getResourceAsStream("../ehealth_smp_acc_truststore.jks"), TEST_KEYSTORE_PASSWORD.toCharArray());

            multiDomainCertificateProvider.getTrustStore(DomainService.DEFAULT_DOMAIN);
            result = truststore;

        }};

        // This entry is valid
        //EndpointInfo endpointInfo = dynamicDiscoveryServiceOASIS.lookupInformation("0007:9340033829test2", "ehealth-actorid-qns", "busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1", "urn:www.cenbii.eu:profile:bii05:ver2.0", "cenbii-procid-ubl");

        // This entry is valid but has no certificate
        //EndpointInfo endpointInfo = dynamicDiscoveryServiceOASIS.lookupInformation("0007:9340033829dev1", "ehealth-actorid-qns", "busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.2", "urn:www.cenbii.eu:profile:bii05:ver2.0", "cenbii-procid-ubl");

        //TEST Service
        //EndpointInfo endpointInfo = dynamicDiscoveryServiceOASIS.lookupInformation("0007:9340033829test2", "ehealth-actorid-qns", "busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1", "urn:www.cenbii.eu:profile:bii05:ver2.0", "cenbii-procid-ubl");

        EndpointInfo endpointInfo = dynamicDiscoveryServiceOASIS.lookupInformation("0088:270420181111", "iso6523-actorid-upis", "busdox-docid-qns::lululu", "urn:www.cenbii.eu:profile:bii04:ver1.0", "cenbii-procid-ubl");

        // Support Issue
        //EndpointInfo endpointInfo = dynamicDiscoveryServiceOASIS.lookupInformation("dynceftestparty13gw", "connectivity-partid-qns", "connectivity-docid-qns::doc_id1", "urn:www.cenbii.eu:profile:bii04:ver1.0", "connectivity-docid-qns");

        System.out.println(endpointInfo.getAddress());
        System.out.println(endpointInfo.getCertificate());
        Assert.assertNotNull(endpointInfo);
    }

}