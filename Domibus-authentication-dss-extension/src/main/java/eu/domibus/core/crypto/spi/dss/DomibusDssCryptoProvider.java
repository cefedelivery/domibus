package eu.domibus.core.crypto.spi.dss;

import eu.domibus.core.crypto.spi.AbstractCryptoServiceSpi;
import eu.europa.esig.dss.tsl.TLInfo;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.x509.CertificateSource;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CommonCertificateSource;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Thomas Dussart
 * @since 4.1
 *
 * Future DSS IAM provider implementation.
 */

@Component
public class DomibusDssCryptoProvider extends AbstractCryptoServiceSpi{

    private static final Logger LOG = LoggerFactory.getLogger(DomibusDssCryptoProvider.class);

    @Autowired
    private CertificateVerifier certificateVerifier;

    @Autowired
    private TSLRepository tslRepository;

    @Override
    public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {
        final Map<String, TLInfo> summary = tslRepository.getSummary();
        for (Map.Entry<String, TLInfo> stringTLInfoEntry : summary.entrySet()) {
            LOG.info("Key:[{}], info:[{}]",stringTLInfoEntry.getKey(),stringTLInfoEntry.getValue());
        }
        CertificateSource adjunctCertSource = new CommonCertificateSource();
        X509Certificate leafCertfiicate = certs[0];
        for (int i = 0; i < certs.length; i++) {
            if (i == 0) {
                continue;
            }
            CertificateToken certificateToken = new CertificateToken(certs[i]);
            adjunctCertSource.addCertificate(certificateToken);

        }
        certificateVerifier.setAdjunctCertSource(adjunctCertSource);
        CertificateValidator certificateValidator = CertificateValidator.fromCertificate(new CertificateToken(leafCertfiicate));
        certificateValidator.setCertificateVerifier(certificateVerifier);
        certificateValidator.setValidationTime(new Date(System.currentTimeMillis()));

        CertificateReports reports = certificateValidator.validate();
        reports.print();
    }

    @Override
    public String getIdentifier() {
        return "DSS_CRYPTO_PROVIDER";
    }
}
