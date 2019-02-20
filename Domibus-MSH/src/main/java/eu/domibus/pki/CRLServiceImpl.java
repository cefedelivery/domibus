package eu.domibus.pki;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CRLServiceImpl implements CRLService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CRLServiceImpl.class);

    public static final String CRL_EXCLUDED_PROTOCOLS = "domibus.certificate.crl.excludedProtocols";

    @Autowired
    protected CRLUtil crlUtil;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    private volatile List<String> supportedCrlProtocols;

    private Object supportedCrlProtocolsLock = new Object();

    @Override
    public boolean isCertificateRevoked(X509Certificate cert) throws DomibusCRLException {
        List<String> crlDistributionPoints = crlUtil.getCrlDistributionPoints(cert);

        LOG.debug("CRL check for certificate: [{}]", getSubjectDN(cert));
        if (crlDistributionPoints == null || crlDistributionPoints.isEmpty()) {
            LOG.debug("No CRL distribution points found for certificate: [{}]", getSubjectDN(cert));
            return false;
        }

        List<String> supportedCrlDistributionPoints = getSupportedCrlDistributionPoints(crlDistributionPoints);
        if (supportedCrlDistributionPoints.isEmpty()) {
            throw new DomibusCRLException("No supported CRL distribution point found for certificate " + getSubjectDN(cert));
        }

        for (String crlDistributionPointUrl : supportedCrlDistributionPoints) {
            try {
                // once checked, stop checking, no matter if the outcome was true or false
                return isCertificateRevoked(cert, crlDistributionPointUrl);
            } catch (DomibusCRLException ex) {
                LOG.warn("Could not check certificate against CRL url [{}]", crlDistributionPointUrl, ex);
                continue; // for clarity: continue with the next CRL url, until one usable is found
            }
        }

        throw new DomibusCRLException("Could not check certificate " + getSubjectDN(cert) + " against any CRL distribution point");
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
            if (isURLSupported(crlDistributionPoint)) {
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

    /**
     * Checks the pki revocation status against the provided distribution point.
     * Supports HTTP, HTTPS, FTP, File based URLs.
     * @param serialString the pki serial number
     * @param crlDistributionPointURL the certificate revocation list url
     * @return true if the pki is revoked
     * @throws DomibusCRLException if an error occurs while downloading the certificate revocation list
     */
    boolean isCertificateRevoked(String serialString, String crlDistributionPointURL) throws DomibusCRLException {
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

    private boolean isURLSupported(final String crlURL) {
        if (!CRLUrlType.isURLSupported(crlURL)) {
            return false;
        }
        for (String crlProtocol : getSupportedCrlProtocols()) {
            if (crlURL.toLowerCase().startsWith(crlProtocol)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getSupportedCrlProtocols() {
        if (supportedCrlProtocols == null) {
            synchronized (supportedCrlProtocolsLock) {
                if (supportedCrlProtocols == null) {
                    List<String> list = Arrays.stream(CRLUrlType.values()).map(c -> c.getPrefix()).collect(Collectors.toList());
                    final String excludedProtocolsList = domibusPropertyProvider.getProperty(CRL_EXCLUDED_PROTOCOLS);
                    if (!StringUtils.isEmpty(excludedProtocolsList)) {
                        List<String> excluded = Arrays.stream(excludedProtocolsList.split(",")).map(p -> p.trim() + "://").collect(Collectors.toList());
                        list.removeAll(excluded);
                    }
                    supportedCrlProtocols = list;
                }
            }
        }
        return supportedCrlProtocols;
    }

}
