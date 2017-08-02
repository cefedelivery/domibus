package eu.domibus.pki;

import eu.domibus.wss4j.common.crypto.CryptoService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Cosmin Baciu on 07-Jul-16.
 */
@RunWith(JMockit.class)
public class CertificateServiceImplTest {

    @Tested
    CertificateServiceImpl certificateService;

    @Injectable
    private Properties domibusProperties;

    @Injectable
    CRLService crlService;

    @Injectable
    CryptoService cryptoService;

    PKIUtil pkiUtil = new PKIUtil();

    @Before
    public void init() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Test
    public void testIsCertificateChainValid(@Injectable final KeyStore trustStore) throws Exception {
        final String receiverAlias = "red_gw";

        final X509Certificate rootCertificate = pkiUtil.createCertificate(BigInteger.ONE, null);
        final X509Certificate receiverCertificate = pkiUtil.createCertificate(BigInteger.ONE, null);

        new Expectations(certificateService) {{
            cryptoService.getTrustStore();
            result = trustStore;

            trustStore.getCertificateChain(receiverAlias);
            X509Certificate[] certificateChain = new X509Certificate[]{receiverCertificate, rootCertificate};
            result = certificateChain;

            certificateService.isCertificateValid(rootCertificate);
            result = true;

            certificateService.isCertificateValid(receiverCertificate);
            result = true;
        }};

        boolean certificateChainValid = certificateService.isCertificateChainValid(receiverAlias);
        assertTrue(certificateChainValid);

        new Verifications() {{ // a "verification block"
            // Verifies an expected invocation:
            certificateService.isCertificateValid(rootCertificate);
            times = 1;
            certificateService.isCertificateValid(receiverCertificate);
            times = 1;
        }};
    }

    @Test
    public void testIsCertificateChainValidWithNotValidCertificateRoot(@Injectable final KeyStore trustStore) throws Exception {
        final String receiverAlias = "red_gw";

        final X509Certificate rootCertificate = pkiUtil.createCertificate(BigInteger.ONE, null);
        final X509Certificate receiverCertificate = pkiUtil.createCertificate(BigInteger.ONE, null);

        new Expectations(certificateService) {{
            cryptoService.getTrustStore();
            result = trustStore;

            trustStore.getCertificateChain(receiverAlias);
            X509Certificate[] certificateChain = new X509Certificate[]{receiverCertificate, rootCertificate};
            result = certificateChain;

            certificateService.isCertificateValid(receiverCertificate);
            result = false;
        }};

        boolean certificateChainValid = certificateService.isCertificateChainValid(receiverAlias);
        assertFalse(certificateChainValid);

        new Verifications() {{
            certificateService.isCertificateValid(receiverCertificate);
            times = 1;

            certificateService.isCertificateValid(rootCertificate);
            times = 0;
        }};
    }

    @Test
    public void testIsCertificateValid(@Mocked final X509Certificate certificate) throws Exception {
        new Expectations(certificateService) {{
            certificateService.checkValidity(certificate);
            result = true;

            crlService.isCertificateRevoked(certificate);
            result = false;
        }};

        boolean certificateValid = certificateService.isCertificateValid(certificate);
        assertTrue(certificateValid);
    }

    @Test
    public void testIsCertificateValidWithExpiredCertificate(@Mocked final X509Certificate certificate) throws Exception {
        new Expectations(certificateService) {{
            certificateService.checkValidity(certificate);
            result = false;
        }};

        boolean certificateValid = certificateService.isCertificateValid(certificate);
        assertFalse(certificateValid);
    }

    @Test
    public void testCheckValidityValidWithExpiredCertificate() throws Exception {
        X509Certificate x509Certificate = pkiUtil.createCertificate(BigInteger.ONE, new DateTime().minusDays(2).toDate(), new DateTime().minusDays(1).toDate(), null);
        boolean certificateValid = certificateService.checkValidity(x509Certificate);
        assertFalse(certificateValid);
    }

    @Test
    public void testCheckValidityWithNotYetValidCertificate() throws Exception {
        X509Certificate x509Certificate = pkiUtil.createCertificate(BigInteger.ONE, new DateTime().plusDays(2).toDate(), new DateTime().plusDays(5).toDate(), null);

        boolean certificateValid = certificateService.checkValidity(x509Certificate);
        assertFalse(certificateValid);
    }
}
