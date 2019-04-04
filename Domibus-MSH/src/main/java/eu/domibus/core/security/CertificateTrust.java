package eu.domibus.core.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Wrapper around a signing certificate and its trust chain.
 */
public class CertificateTrust {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateTrust.class);

    private X509Certificate signingCertificate;

    private List<X509Certificate> trustChain;

    public CertificateTrust(X509Certificate signingCertificate, List<X509Certificate> trustChain) {
        this.signingCertificate = signingCertificate;
        this.trustChain = trustChain;
    }

    public X509Certificate getSigningCertificate() {
        return signingCertificate;
    }

    public List<X509Certificate> getTrustChain() {
        return trustChain;
    }
}
