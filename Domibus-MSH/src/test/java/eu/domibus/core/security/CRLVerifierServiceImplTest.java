package eu.domibus.core.security;

import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.security.ICRLVerifierService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateServiceImpl;
import mockit.Tested;
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
public class CRLVerifierServiceImplTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CRLVerifierServiceImplTest.class);

    private CertificateServiceImpl certificateService = new CertificateServiceImpl();

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
    private static final String TEST_KEYSTORE = "testkeystore.jks";
    private static final String ALIAS_CN_AVAILABLE = "cn_available";
    private static final String TEST_KEYSTORE_PASSWORD = "1234";

    @Tested
    CRLVerifierServiceImpl securityCRLVerifierServiceImpl;

    @Test
    public void verifyCertificateCRLsTest() {
        X509Certificate certificate = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        assertNotNull(certificate);
        securityCRLVerifierServiceImpl.verifyCertificateCRLs(certificate);
    }

}
