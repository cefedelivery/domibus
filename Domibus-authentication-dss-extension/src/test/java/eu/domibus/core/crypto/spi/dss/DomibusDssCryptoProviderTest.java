package eu.domibus.core.crypto.spi.dss;

import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.europa.esig.dss.jaxb.detailedreport.DetailedReport;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.x509.CertificateToken;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@RunWith(JMockit.class)
public class DomibusDssCryptoProviderTest {

    @org.junit.Test(expected = WSSecurityException.class)
    public void verifyTrustNoChain(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                   @Mocked CertificateVerifier certificateVerifier,
                                   @Mocked TSLRepository tslRepository,
                                   @Mocked ValidationReport validationReport,
                                   @Mocked X509Certificate leafCertificate) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {leafCertificate};
        final DomibusDssCryptoProvider domibusDssCryptoProvider = new DomibusDssCryptoProvider(defaultDomainCryptoService, certificateVerifier, tslRepository, validationReport, null);
        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
        assertTrue(false);
    }

    @org.junit.Test(expected = WSSecurityException.class)
    public void verifyTrustNoLeafCertificate(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                             @Mocked CertificateVerifier certificateVerifier,
                                             @Mocked TSLRepository tslRepository,
                                             @Mocked ValidationReport validationReport,
                                             @Mocked X509Certificate noLeafCertificate,
                                             @Mocked X509Certificate chainCertificate) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {noLeafCertificate, chainCertificate};

        new Expectations() {{
            noLeafCertificate.getBasicConstraints();
            result = 0;
            chainCertificate.getBasicConstraints();
            result = 0;
        }};
        final DomibusDssCryptoProvider domibusDssCryptoProvider = new DomibusDssCryptoProvider(defaultDomainCryptoService, certificateVerifier, tslRepository, validationReport, null);
        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
        assertTrue(false);
    }

    @org.junit.Test(expected = WSSecurityException.class)
    public void verifyTrustNotValid(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                    @Mocked CertificateVerifier certificateVerifier,
                                    @Mocked TSLRepository tslRepository,
                                    @Mocked ValidationReport validationReport,
                                    @Mocked ValidationConstraintPropertyMapper constraintMapper,
                                    @Mocked X509Certificate noLeafCertificate,
                                    @Mocked X509Certificate chainCertificate,
                                    @Mocked CertificateValidator certificateValidator,
                                    @Mocked CertificateReports reports,
                                    @Mocked DetailedReport detailedReport) throws WSSecurityException {
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
            reports.getDetailedReportJaxb();
            result = detailedReport;

        }};
        final DomibusDssCryptoProvider domibusDssCryptoProvider = new DomibusDssCryptoProvider(defaultDomainCryptoService, certificateVerifier, tslRepository, validationReport, constraintMapper);
        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
        assertTrue(false);
        new Verifications() {{
            List constraints = new ArrayList<>();
            validationReport.isValid(detailedReport, withAny(constraints));
            times = 1;
        }};

    }

    @Test
    public void verifyTrustValid(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                 @Mocked CertificateVerifier certificateVerifier,
                                 @Mocked TSLRepository tslRepository,
                                 @Mocked ValidationReport validationReport,
                                 @Mocked ValidationConstraintPropertyMapper constraintMapper,
                                 @Mocked X509Certificate noLeafCertificate,
                                 @Mocked X509Certificate chainCertificate,
                                 @Mocked CertificateValidator certificateValidator,
                                 @Mocked CertificateReports reports,
                                 @Mocked DetailedReport detailedReport) throws WSSecurityException {
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

            reports.getDetailedReportJaxb();
            result = detailedReport;

            List constraints = new ArrayList<>();
            validationReport.isValid(detailedReport, withAny(constraints));
            result = true;

        }};
        final DomibusDssCryptoProvider domibusDssCryptoProvider = new DomibusDssCryptoProvider(defaultDomainCryptoService, certificateVerifier, tslRepository, validationReport, constraintMapper);
        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);

    }


}