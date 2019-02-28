package eu.domibus.core.crypto.spi.dss;

import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.europa.esig.dss.jaxb.simplecertificatereport.SimpleCertificateReport;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.x509.CertificateToken;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.cert.X509Certificate;

import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class DomibusDssCryptoProviderTest {

    @Injectable
    private CertificateVerifier certificateVerifier;

    @Injectable
    private TSLRepository tslRepository;

    @Injectable
    private SimpleReportValidator simpleReportValidator;

    @Injectable
    private DomainCryptoServiceSpi defaultDomainCryptoService;

    @Tested
    private DomibusDssCryptoProvider domibusDssCryptoProvider;

    @org.junit.Test(expected = WSSecurityException.class)
    public void verifyTrustNoChain(@Mocked X509Certificate leafCertificate) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {leafCertificate};
        try {
            domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
            assertTrue(false);
        } catch (WSSecurityException e) {
            throw e;
            //@todo check what is going on here.
            //assertEquals("The chain of certificate should contain at least two certificates",e.getMessage());
        }
    }

    @org.junit.Test(expected = WSSecurityException.class)
    public void verifyTrustNoLeafCertificate(@Mocked X509Certificate noLeafCertificate,
                                             @Mocked X509Certificate chainCertificate) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {noLeafCertificate, chainCertificate};

        new Expectations() {{
            noLeafCertificate.getBasicConstraints();
            result = 0;
            chainCertificate.getBasicConstraints();
            result = 0;
        }};
        try {
            domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
            assertTrue(false);
        } catch (WSSecurityException e) {
            throw e;
            //@todo check what is going on here.
            //assertEquals("The chain of certificate should contain at least two certificates",e.getMessage());
        }
    }

    @org.junit.Test(expected = WSSecurityException.class)
    public void verifyTrustNotValid(@Mocked X509Certificate noLeafCertificate,
                                    @Mocked X509Certificate chainCertificate,
                                    @Mocked CertificateValidator certificateValidator,
                                    @Mocked CertificateReports reports,
                                    @Mocked SimpleCertificateReport simpleCertificateReport) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {noLeafCertificate, chainCertificate};
        org.apache.xml.security.Init.init();

        new Expectations() {{
            noLeafCertificate.getBasicConstraints();
            result = -1;
            noLeafCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            chainCertificate.getBasicConstraints();
            result = 0;
            chainCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            certificateValidator.validate();
            result = reports;
            reports.getSimpleReportJaxb();
            result = simpleCertificateReport;

        }};
        try {
            domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
            assertTrue(false);
        } catch (WSSecurityException e) {
            throw e;
        }
        new Verifications() {{
            simpleReportValidator.isValid(simpleCertificateReport);
            times = 1;
        }};

    }

    @Test
    public void verifyTrustValid(@Mocked X509Certificate noLeafCertificate,
                                 @Mocked X509Certificate chainCertificate,
                                 @Mocked CertificateValidator certificateValidator,
                                 @Mocked CertificateReports reports,
                                 @Mocked SimpleCertificateReport simpleCertificateReport) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {noLeafCertificate, chainCertificate};
        org.apache.xml.security.Init.init();

        new Expectations() {{
            noLeafCertificate.getBasicConstraints();
            result = -1;
            noLeafCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            chainCertificate.getBasicConstraints();
            result = 0;
            chainCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            certificateValidator.validate();
            result = reports;

            reports.getSimpleReportJaxb();
            result = simpleCertificateReport;

            simpleReportValidator.isValid(simpleCertificateReport);
            result = true;

        }};
        try {
            domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
        } catch (WSSecurityException e) {
            assertTrue(false);
        }

    }


}