package eu.domibus.pki;

import com.google.common.collect.Lists;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.common.model.certificate.Certificate;
import eu.domibus.common.model.certificate.CertificateStatus;
import eu.domibus.common.model.certificate.CertificateType;
import eu.domibus.core.certificate.CertificateDao;
import eu.domibus.logging.DomibusLogger;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static eu.domibus.logging.DomibusMessageCode.SEC_CERTIFICATE_REVOKED;
import static eu.domibus.logging.DomibusMessageCode.SEC_CERTIFICATE_SOON_REVOKED;
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

    @Injectable
    CertificateDao certificateDao;

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

    @Test
    public void saveCertificateAndLogRevocation(){
        certificateService.saveCertificateAndLogRevocation();
        new Verifications(){{
           certificateService.saveCertificateData(); times=1;
           certificateService.logCertificateRevocationWarning();times=1;
        }};
    }

    @Test
    public void saveCertificateData(){
        final Certificate cert1 = new Certificate();
        final Certificate cert2 = new Certificate();
        final List<Certificate> certificates=Lists.newArrayList(cert1, cert2);
        new Expectations(certificateService){{
            certificateService.groupAllKeystoreCertificates();
            result=certificates;
        }};
        certificateService.saveCertificateData();
        new Verifications(){{
            certificateDao.saveOrUpdate(withInstanceOf(Certificate.class));times=2;
        }};
    }

    @Test
    public void logCertificateRevocationWarning(@Mocked final DomibusLogger LOG){
        final Certificate soonRevokedCertificate = new Certificate();
        final Date now = new Date();
        final String soonRevokedAlias = "Cert1";
        soonRevokedCertificate.setNotAfter(now);
        soonRevokedCertificate.setAlias(soonRevokedAlias);
        final List<Certificate> unNotifiedSoonRevokedCertificates= Lists.newArrayList(soonRevokedCertificate);

        final String revokedAlias = "Cert2";
        final Certificate revokedCertificate = new Certificate();
        revokedCertificate.setNotAfter(now);
        revokedCertificate.setAlias(revokedAlias);
        final List<Certificate> unNotifiedRevokedCertificates= Lists.newArrayList(revokedCertificate);

        new Expectations(){{
            certificateDao.getUnNotifiedSoonRevoked();
            result=unNotifiedSoonRevokedCertificates;
            certificateDao.getUnNotifiedRevoked();
            result=unNotifiedRevokedCertificates;
        }};
        certificateService.logCertificateRevocationWarning();

        new Verifications(){{
            LOG.securityWarn(SEC_CERTIFICATE_SOON_REVOKED, soonRevokedAlias, now);times=1;
            LOG.securityError(SEC_CERTIFICATE_REVOKED, revokedAlias, now);times=1;
            certificateDao.updateRevocation(soonRevokedCertificate);times=1;
            certificateDao.updateRevocation(revokedCertificate);times=1;
        }};
    }
    @Test
    public void retrieveCertificates(@Mocked final KeyStore keyStore,@Mocked final KeyStore trustStore){

        Certificate certificate = new Certificate();
        certificate.setNotAfter(new Date());
        final List<Certificate> trustStoreCertificates= Lists.newArrayList(certificate);
        certificate = new Certificate();
        certificate.setNotAfter(new Date());
        final List<Certificate> keyStoreCertificates= Lists.newArrayList(certificate);

        new Expectations(certificateService){{
            cryptoService.getTrustStore();
            result=trustStore;
            cryptoService.getKeyStore();
            result=keyStore;
            certificateService.extractCertificateFromKeyStore(trustStore);
            result=trustStoreCertificates;
            certificateService.extractCertificateFromKeyStore(keyStore);
            result=keyStoreCertificates;
        }};

        List<Certificate> certificates = certificateService.groupAllKeystoreCertificates();
        assertEquals(CertificateType.PUBLIC,certificates.get(0).getCertificateType());
        assertEquals(CertificateType.PRIVATE,certificates.get(1).getCertificateType());

    }

    @Test
    public void updateCertificateStatus(){
        Date now=new Date();

        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DATE, 11);
        CertificateStatus certificateStatus = certificateService.getCertificateStatus(c.getTime());
        assertEquals(CertificateStatus.OK,certificateStatus);

        c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DATE, 9);
        certificateStatus = certificateService.getCertificateStatus(c.getTime());
        assertEquals(CertificateStatus.SOON_REVOKED,certificateStatus);

        c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DATE, -1);
        certificateStatus = certificateService.getCertificateStatus(c.getTime());
        assertEquals(CertificateStatus.REVOKED,certificateStatus);
    }


    @Test
    public void extractCertificateFromKeyStore(@Mocked final KeyStore keyStore,
                                               @Mocked final Enumeration<String> aliases,
                                               @Mocked final X509Certificate x509Certificate) throws KeyStoreException, ParseException {
        final String keystoreAlias = "keystoreAlias";
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        final Date notBefore = format.parse("2017/02/20");
        final Date notAfter = format.parse("2017/04/20");

        new Expectations(){{

            keyStore.aliases();
            result=aliases;

            aliases.hasMoreElements();times=2;
            result=true;
            result=false;

            aliases.nextElement();times=1;
            result= keystoreAlias;

            keyStore.getCertificate(keystoreAlias);
            result=x509Certificate;

            x509Certificate.getNotBefore();
            result=notBefore;

            x509Certificate.getNotAfter();
            result=notAfter;

        }};

        List<Certificate> certificates = certificateService.extractCertificateFromKeyStore(keyStore);
        assertEquals(1,certificates.size());
        assertEquals(certificates.get(0).getNotBefore(),notBefore);
        assertEquals(certificates.get(0).getNotAfter(),notAfter);
    }
}
