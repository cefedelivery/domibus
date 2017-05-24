package eu.domibus.pki;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;


@Service
public class CRLServiceImpl implements CRLService {

    private static final Log LOG = LogFactory.getLog(CRLServiceImpl.class);

    @Autowired
    protected CRLUtil crlUtil;

    @Override
    public boolean isCertificateRevoked(X509Certificate cert) throws DomibusCRLException {
        List<String> crlDistributionPoints = crlUtil.getCrlDistributionPoints(cert);
        List<String> supportedCrlDistributionPoints = new ArrayList<>();

        for (String crlDistributionPoint : crlDistributionPoints) {
            if (crlUtil.isURLSupported(crlDistributionPoint)) {
                supportedCrlDistributionPoints.add(crlDistributionPoint);
            }
        }

        if (supportedCrlDistributionPoints.size() == 0) {
            throw new DomibusCRLException("No supported CRL distribution point found for certificate " + cert.getSubjectDN().getName());
        }

        for (String crlDistributionPointUrl : supportedCrlDistributionPoints) {
            if (isCertificateRevoked(cert, crlDistributionPointUrl)) {
                return true;
            }
        }

        return false;
    }

    protected boolean isCertificateRevoked(X509Certificate cert, String crlDistributionPointURL) {
        X509CRL crl = crlUtil.downloadCRL(crlDistributionPointURL);
        if (crl.isRevoked(cert)) {
            LOG.debug("The pki is revoked by CRL: " + crlDistributionPointURL);
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
