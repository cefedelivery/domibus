package eu.domibus.pki;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;


@Service
public class CRLServiceImpl implements CRLService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CRLServiceImpl.class);

    @Autowired
    protected CRLUtil crlUtil;

    @Override
    public boolean isCertificateRevoked(X509Certificate cert) throws DomibusCRLException {
        List<String> crlDistributionPoints = crlUtil.getCrlDistributionPoints(cert);

        if (crlDistributionPoints == null || crlDistributionPoints.isEmpty()) {
            LOG.debug("No CRL distribution points found for certificate: [" + getSubjectDN(cert) + "]");
            return false;
        }

        List<String> supportedCrlDistributionPoints = getSupportedCrlDistributionPoints(crlDistributionPoints);
        if (supportedCrlDistributionPoints.isEmpty()) {
            throw new DomibusCRLException("No supported CRL distribution point found for certificate " + getSubjectDN(cert));
        }

        for (String crlDistributionPointUrl : supportedCrlDistributionPoints) {
            if (isCertificateRevoked(cert, crlDistributionPointUrl)) {
                return true;
            }
        }

        return false;
    }

    protected String getSubjectDN(X509Certificate cert) {
        if (cert != null && cert.getSubjectDN() != null) {
            return cert.getSubjectDN().getName();
        }
        return null;
    }

    protected List<String> getSupportedCrlDistributionPoints(List<String> crlDistributionPoints) {
        List<String> result = new ArrayList<>();
        if (crlDistributionPoints == null || crlDistributionPoints.isEmpty()) {
            return result;
        }

        for (String crlDistributionPoint : crlDistributionPoints) {
            if (crlUtil.isURLSupported(crlDistributionPoint)) {
                result.add(crlDistributionPoint);
            } else {
                LOG.debug("The protocol of the distribution endpoint is not supported: " + crlDistributionPoint);
            }
        }

        return result;
    }

    protected boolean isCertificateRevoked(X509Certificate cert, String crlDistributionPointURL) {
        X509CRL crl = crlUtil.downloadCRL(crlDistributionPointURL);
        if (crl.isRevoked(cert)) {
            LOG.warn("The certificate is revoked by CRL: " + crlDistributionPointURL);
            return true;
        }
        return false;
    }

    @Override
    public boolean isCertificateRevoked(String serialString, String crlDistributionPointURL) throws DomibusCRLException {
        X509CRL crl = crlUtil.downloadCRL(crlDistributionPointURL);

        if (crl.getRevokedCertificates() == null) {
            LOG.debug("The CRL is null for the given pki");
            return false;
        }

        BigInteger certificateSerial = crlUtil.parseCertificateSerial(serialString);
        for (X509CRLEntry entry : crl.getRevokedCertificates()) {
            if (certificateSerial.equals(entry.getSerialNumber())) {
                LOG.debug("The pki is revoked by CRL: " + crlDistributionPointURL);
                return true;
            }
        }
        return false;
    }

}
