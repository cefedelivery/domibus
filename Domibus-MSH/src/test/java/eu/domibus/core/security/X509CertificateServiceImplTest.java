package eu.domibus.core.security;

import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.security.ICRLVerifierService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateServiceImpl;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertNotNull;

/**
 * @author idragusa
 * @since 4.0
 */
@RunWith(JMockit.class)
public class X509CertificateServiceImplTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(X509CertificateServiceImplTest.class);

    private CertificateServiceImpl certificateService = new CertificateServiceImpl();

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
    private static final String TEST_KEYSTORE = "testkeystore.jks";
    private static final String ALIAS_CN_AVAILABLE = "cn_available";
    private static final String TEST_KEYSTORE_PASSWORD = "1234";

    private static final String EXPIRED_KEYSTORE = "expired_gateway_keystore.jks";
    private static final String EXPIRED_ALIAS = "blue_gw";
    private static final String EXPIRED_KEYSTORE_PASSWORD = "test123";


    @Tested
    X509CertificateServiceImpl securityX509CertificateServiceImpl;

    @Injectable
    ICRLVerifierService securityCRLVerifierServiceImpl;

    @Test
    public void verifyCertificateTest() {
        X509Certificate[] certificates = createCertificates(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        securityX509CertificateServiceImpl.isClientX509CertificateValid(certificates);

        new Verifications() {{
            securityCRLVerifierServiceImpl.verifyCertificateCRLs(certificates[0]);
            times = 1;
        }};
    }

    @Test(expected = AuthenticationException.class)
    public void verifyCertificateExpiredTest() {
        X509Certificate[] certificates = createCertificates(RESOURCE_PATH + EXPIRED_KEYSTORE, EXPIRED_ALIAS, EXPIRED_KEYSTORE_PASSWORD);
        securityX509CertificateServiceImpl.isClientX509CertificateValid(certificates);

        new Verifications() {{
            securityCRLVerifierServiceImpl.verifyCertificateCRLs(certificates[0]);
            times = 0;
        }};
    }

    private X509Certificate[] createCertificates(String keystore_path, String alias, String password) {
        X509Certificate certificate = certificateService.loadCertificateFromJKSFile(keystore_path, alias, password);
        assertNotNull(certificate);
        X509Certificate[] certificates = new X509Certificate[1];
        certificates[0] = certificate;
        return certificates;
    }
}
