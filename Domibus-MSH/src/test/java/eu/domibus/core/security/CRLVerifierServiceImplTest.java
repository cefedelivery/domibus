package eu.domibus.core.security;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateServiceImpl;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;
import java.math.BigInteger;
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

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/core/security/";
    private static final String TEST_KEYSTORE = "gateway_keystore_crl.jks";
    private static final String TEST_KEYSTORE_INVALID = "gateway_keystore_crl_invalid.jks";
    private static final String ALIAS = "ut";
    private static final String ALIAS_INVALID = "edelivery";
    private static final String TEST_KEYSTORE_PASSWORD = "test123";

    @Tested
    CRLVerifierServiceImpl securityCRLVerifierServiceImpl;

    @Test
    public void verifyCertificateCRLsTest() throws IOException {
        X509Certificate certificate = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS, TEST_KEYSTORE_PASSWORD);
        assertNotNull(certificate);
        securityCRLVerifierServiceImpl.verifyCertificateCRLs(certificate);
    }

    @Test
    public void verifyCertificateCRLsSerialTest() throws IOException {
        X509Certificate certificate = certificateService.loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE_INVALID, ALIAS_INVALID, TEST_KEYSTORE_PASSWORD);
        assertNotNull(certificate);
        String serial = certificate.getSerialNumber().toString();
        securityCRLVerifierServiceImpl.verifyCertificateCRLs(serial, "test.crl");
    }
}
