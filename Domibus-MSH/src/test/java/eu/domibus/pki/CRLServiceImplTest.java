package eu.domibus.pki;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.math.BigInteger;
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Cosmin Baciu on 07-Jul-16.
 */
@RunWith(JMockit.class)
public class CRLServiceImplTest {

    @Tested
    CRLServiceImpl crlService;

    @Injectable
    CRLUtil crlUtil;

    @Injectable
    X509CRL x509CRL;

    PKIUtil pkiUtil = new PKIUtil();

    @Before
    public void init() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    //    @Test
    public void testCreateCertificate() throws Exception {
        BigInteger serial = new BigInteger("0400000000011E44A5E404", 16);
        X509Certificate certificate = pkiUtil.createCertificate(serial, Arrays.asList(new String[]{"test.crl", "test1.crl"}));
        System.out.println(certificate);
        FileUtils.writeByteArrayToFile(new File("c:\\work\\certificates_self_signed\\mycertificate.cer"), certificate.getEncoded());
    }

    //    @Test
    public void testGenerateCRL() throws Exception {
        X509CRL crl = pkiUtil.createCRL(Arrays.asList(new BigInteger[]{new BigInteger("0400000000011E44A5E405", 16), new BigInteger("0400000000011E44A5E404", 16)}));
        FileUtils.writeByteArrayToFile(new File("c:\\work\\certificates_self_signed\\mycrl.crl"), crl.getEncoded());
    }

    @Test
    public void testIsCertificateRevoked(@Injectable final X509Certificate certificate) throws Exception {
        BigInteger serial = new BigInteger("0400000000011E44A5E404", 16);
        final String crlUrl1 = "http://domain1.crl";
        final String crlUrl2 = "http://domain2.crl";
        final List<String> crlUrlList = Arrays.asList(new String[]{crlUrl1, crlUrl2});

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList, crlUrlList);

            crlUtil.isURLSupported(anyString);
            returns(true, true);

            crlService.isCertificateRevoked(certificate, crlUrl1);
            returns(false, true, false);

            crlService.isCertificateRevoked(certificate, crlUrl2);
            returns(false, true, true);
        }};
        //certificate is valid in both CRLs
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertFalse(certificateRevoked);

        //certificate is revoked in both CRLs
        certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertTrue(certificateRevoked);

        //certificate is revoked in the second CRL
        certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertTrue(certificateRevoked);
    }

    @Test(expected = DomibusCRLException.class)
    public void testIsCertificateRevokedWithNotSupportedCRLURLs(@Injectable final X509Certificate certificate) throws Exception {
        final String crlUrl1 = "ldap://domain1.crl";
        final String crlUrl2 = "ldap://domain2.crl";
        final List<String> crlUrlList = Arrays.asList(new String[]{crlUrl1, crlUrl2});

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);
        }};
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertFalse(certificateRevoked);
    }

    @Test
    public void testIsCertificateRevokedWhenCertificateHasNoCRLURLs(@Injectable final X509Certificate certificate) throws Exception {
        final List<String> crlUrlList = new ArrayList<>();

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);
        }};
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertFalse(certificateRevoked);
    }

    @Test
    public void testIsCertificateRevokedBySerialNumber(@Injectable final X509CRLEntry x509CRLEntry) throws Exception {
        final String crlUrlString = "file://test";
        final String serialNumber = "0400000000011E44A5E405";
        final BigInteger serialNumberInteger = new BigInteger(serialNumber, 16);
//        final X509CRL x509CRL = pkiUtil.createCRL(Arrays.asList(new BigInteger[]{new BigInteger("0400000000011E44A5E405", 16), new BigInteger("0400000000011E44A5E404", 16)}));
        new Expectations() {{
            crlUtil.downloadCRL(crlUrlString);
            result = x509CRL;

            x509CRLEntry.getSerialNumber();
            result = serialNumberInteger;

            Set x509CRLEntries = new HashSet<>();
            x509CRLEntries.add(x509CRLEntry);
            x509CRL.getRevokedCertificates();
            result = x509CRLEntries;

            crlUtil.parseCertificateSerial(serialNumber);
            result = new BigInteger(serialNumber, 16);

            crlUtil.parseCertificateSerial("1400000000011E44A5E405");
            result = new BigInteger("1400000000011E44A5E405", 16);
        }};
        boolean certificateRevoked = crlService.isCertificateRevoked("0400000000011E44A5E405", crlUrlString);
        assertTrue(certificateRevoked);

        certificateRevoked = crlService.isCertificateRevoked("1400000000011E44A5E405", crlUrlString);
        assertFalse(certificateRevoked);
    }

    @Test
    public void testIsCertificateRevokedWithEmptyCRLRevokedCertificateList() throws Exception {
        final String crlUrlString = "file://test";
        final X509CRL x509CRL = pkiUtil.createCRL(null);
        new Expectations() {{
            crlUtil.downloadCRL(crlUrlString);
            result = x509CRL;
        }};
        boolean certificateRevokedStatus = crlService.isCertificateRevoked("0400000000011E44A5E405", crlUrlString);
        assertFalse(certificateRevokedStatus);
    }

    @Test
    public void testIsCertificateRevokedWithCRLExtractedFromCertificate() throws Exception {
        final String crlUrlString = "file://test";
        final X509CRL x509CRL = pkiUtil.createCRL(Arrays.asList(new BigInteger[]{new BigInteger("0400000000011E44A5E405", 16), new BigInteger("0400000000011E44A5E404", 16)}));
        new Expectations() {{
            crlUtil.downloadCRL(crlUrlString);
            result = x509CRL;
        }};
        X509Certificate certificate = pkiUtil.createCertificate(new BigInteger("0400000000011E44A5E405", 16), null);
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate, crlUrlString);
        assertTrue(certificateRevoked);
    }
}
