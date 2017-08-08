package eu.domibus.pki;

import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.wss4j.common.crypto.CryptoService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

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

    @Test
    public void testGetTrustStoreEntries(@Mocked final KeyStore trustStore,
                                         @Mocked final Enumeration<String> aliasEnum,
                                         @Mocked final X509Certificate blueCertificate,
                                         @Mocked final X509Certificate redCertificate) throws KeyStoreException {
        final Date validFrom = LocalDateTime.now().toDate();
        final Date validUntil = LocalDateTime.now().plusDays(10).toDate();
        new Expectations() {{
            aliasEnum.hasMoreElements();
            returns(true, true, false);
            aliasEnum.nextElement();
            returns("blue_gw", "red_gw");

            trustStore.aliases();
            result = aliasEnum;

            cryptoService.getTrustStore();
            times = 1;
            result = trustStore;

            blueCertificate.getSubjectDN().getName();
            result = "C=BE,O=eDelivery,CN=blue_gw";
            blueCertificate.getIssuerDN().getName();
            result = "C=BE,O=eDelivery,CN=blue_gw";
            blueCertificate.getNotBefore();
            result = validFrom;
            blueCertificate.getNotAfter();
            result = validUntil;

            redCertificate.getSubjectDN().getName();
            result = "C=BE,O=eDelivery,CN=red_gw";
            redCertificate.getIssuerDN().getName();
            result = "C=BE,O=eDelivery,CN=red_gw";
            redCertificate.getNotBefore();
            result = validFrom;
            redCertificate.getNotAfter();
            result = validUntil;

            trustStore.getCertificate("blue_gw");
            result = blueCertificate;
            trustStore.getCertificate("red_gw");
            result = redCertificate;
        }};
        final List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries();
        assertEquals(2, trustStoreEntries.size());

        TrustStoreEntry trustStoreEntry = trustStoreEntries.get(0);
        assertEquals("blue_gw", trustStoreEntry.getName());
        assertEquals("C=BE,O=eDelivery,CN=blue_gw", trustStoreEntry.getSubject());
        assertEquals("C=BE,O=eDelivery,CN=blue_gw", trustStoreEntry.getIssuer());
        assertTrue(validFrom.compareTo(trustStoreEntry.getValidFrom()) == 0);
        assertTrue(validUntil.compareTo(trustStoreEntry.getValidUntil()) == 0);

        trustStoreEntry = trustStoreEntries.get(1);
        assertEquals("red_gw", trustStoreEntry.getName());
        assertEquals("C=BE,O=eDelivery,CN=red_gw", trustStoreEntry.getSubject());
        assertEquals("C=BE,O=eDelivery,CN=red_gw", trustStoreEntry.getIssuer());
        assertTrue(validFrom.compareTo(trustStoreEntry.getValidFrom()) == 0);
        assertTrue(validUntil.compareTo(trustStoreEntry.getValidUntil()) == 0);
    }

    @Test
    public void testGetTrustStoreEntriesWithKeyStoreException(@Mocked final KeyStore trustStore) throws KeyStoreException {

        new Expectations() {{
            trustStore.aliases();
            result = new KeyStoreException();

            cryptoService.getTrustStore();
            times = 1;
            result = trustStore;
        }};
        assertEquals(0, certificateService.getTrustStoreEntries().size());
    }
}
