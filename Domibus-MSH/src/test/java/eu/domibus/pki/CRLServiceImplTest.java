package eu.domibus.pki;

import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
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
    DomibusPropertyProvider domibusPropertyProvider;

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
        X509Certificate certificate = pkiUtil.createCertificate(serial, Arrays.asList("test.crl", "test1.crl"));
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
        final List<String> crlUrlList = Arrays.asList(crlUrl1);

        //stubbing static method
        new MockUp<CRLUrlType>() {
            @Mock
            boolean isURLSupported(final String crlURL) {
                return true;
            }
        };

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList, crlUrlList);

            crlService.isCertificateRevoked(certificate, crlUrl1);
            returns(false, true);
        }};
        //certificate is valid
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertFalse(certificateRevoked);

        //certificate is revoked
        certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertTrue(certificateRevoked);
    }

    @Test(expected = DomibusCRLException.class)
    public void testIsCertificateRevokedWithNotSupportedCRLURLs(@Injectable final X509Certificate certificate) throws Exception {
        final String crlUrl1 = "ldap2://domain1.crl";
        final String crlUrl2 = "ldap2://domain2.crl";
        final List<String> crlUrlList = Arrays.asList(crlUrl1, crlUrl2);

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);
        }};
        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertFalse(certificateRevoked);
    }

    @Test(expected = DomibusCRLException.class)
    public void testIsCertificateRevokedWithAllProtocolsExcluded(@Injectable final X509Certificate certificate) throws Exception {
        final String crlUrl1 = "ftp://domain1.crl"; // excluded
        final String crlUrl2 = "ldap2://domain2.crl"; // unknown
        final List<String> crlUrlList = Arrays.asList(crlUrl1, crlUrl2);

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);

            domibusPropertyProvider.getProperty(CRLServiceImpl.CRL_EXCLUDED_PROTOCOLS);
            returns("ftp,http");
        }};
        crlService.isCertificateRevoked(certificate);
    }

    @Test(expected = DomibusCRLException.class)
    public void testIsCertificateRevokedWithCRLNotDownloaded(@Injectable final X509Certificate certificate) throws Exception {
        final String crlUrl1 = "ftp://domain1.crl";
        final List<String> crlUrlList = Arrays.asList(crlUrl1);

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);

            crlUtil.downloadCRL(crlUrl1);
            result = new DomibusCRLException();
        }};
        crlService.isCertificateRevoked(certificate);
    }

    @Test
    public void testIsCertificateRevokedWithSomeProtocolsExcluded(@Injectable final X509Certificate certificate) throws Exception {
        final String crlUrl1 = "ftp://domain1.crl";
        final String crlUrl2 = "http://domain2.crl";
        final List<String> crlUrlList = Arrays.asList(crlUrl1, crlUrl2);

        new Expectations(crlService) {{
            crlUtil.getCrlDistributionPoints(certificate);
            returns(crlUrlList);

            domibusPropertyProvider.getProperty(CRLServiceImpl.CRL_EXCLUDED_PROTOCOLS);
            returns("ftp");
        }};

        crlService.isCertificateRevoked(certificate);

        new Verifications() {{
            crlService.isCertificateRevoked(certificate, crlUrl1);
            times = 0;

            crlService.isCertificateRevoked(certificate, crlUrl2);
            times = 1;
        }};
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