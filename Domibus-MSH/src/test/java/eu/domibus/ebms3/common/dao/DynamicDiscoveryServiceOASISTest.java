package eu.domibus.ebms3.common.dao;

import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.services.impl.DynamicDiscoveryServiceOASIS;
import eu.domibus.common.services.impl.DynamicDiscoveryServicePEPPOL;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * Created by idragusa on 3/27/17.
 */
@RunWith(JMockit.class)
public class DynamicDiscoveryServiceOASISTest {

    private static final String TEST_SML_ZONE = "acc.edelivery.tech.ec.europa.eu";

    private static final String TEST_RECEIVER_ID = "unknownRecipient";
    private static final String TEST_RECEIVER_ID_TYPE = "unknownRecipientType";
    private static final String TEST_ACTION_VALUE = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1";
    private static final String TEST_SERVICE_VALUE = "serviceValue";
    private static final String TEST_SERVICE_TYPE = "serviceType";

    @Injectable
    private Properties domibusProperties;

    @Tested
    DynamicDiscoveryServiceOASIS dynamicDiscoveryServiceOASIS;

    @Test
    public void testLookupInformation() throws Exception {
        new NonStrictExpectations() {{
            domibusProperties.getProperty(DynamicDiscoveryServicePEPPOL.SMLZONE_KEY);
            result = TEST_SML_ZONE;

        }};

        KeyStore truststore;
        try {
            truststore = KeyStore.getInstance("JKS");
            truststore.load(new FileInputStream(new File("/Users/idragusa/_setup/dyn_disc_mixed_with_static/truststoreForTrustedCertificate.ts")), "test".toCharArray());
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException exc ) {
            throw new ConfigurationException("Could not fetch metadata from SMP", exc);
        }

        // TODO add proxy to fetcher
        DynamicDiscovery smpClient = DynamicDiscoveryBuilder.newInstance()
                .locator(new DefaultBDXRLocator("ehealth.acc.edelivery.tech.ec.europa.eu"))
                .reader(new DefaultBDXRReader(new DefaultSignatureValidator(truststore)))
                .build();

//        Endpoint endpoint = dynamicDiscoveryServiceOASIS.lookupInformation("urn:romania:ncpb", "ehealth-actorid-qns", "ehealth-resid-qns:urn::epsos##services:extended:epsos::107", "urn:www.cenbii.eu:profile:bii04:ver1.0", "cenbii-procid-ubl");
//
//        System.out.println(endpoint.getAddress());
    }
}
