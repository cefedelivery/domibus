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
import java.security.cert.X509Certificate;
import java.util.Arrays;

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

    PKIUtil pkiUtil = new PKIUtil();

    @Before
    public void init() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Test
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
    public void testIsCertificateRevoked() throws Exception {
        final String crlUrlString = "file://test";
        final X509CRL x509CRL = pkiUtil.createCRL(Arrays.asList(new BigInteger[]{new BigInteger("0400000000011E44A5E405", 16), new BigInteger("0400000000011E44A5E404", 16)}));
        new Expectations() {{
            crlUtil.downloadCRL(crlUrlString);
            result = x509CRL;
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
    public void isCertificateRevoked1() throws Exception {
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
