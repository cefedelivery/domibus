package eu.domibus.pki;

import javax.naming.InvalidNameException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * @Author Cosmin Baciu
 * @Since 3.2
 */
public interface CertificateService {

    boolean isCertificateValidationEnabled();

    boolean isCertificateValid(X509Certificate cert) throws DomibusCertificateException;

    boolean isCertificateChainValid(String alias) throws DomibusCertificateException;

    boolean isCertificateValid(String alias) throws DomibusCertificateException;

    String extractCommonName(final X509Certificate certificate) throws InvalidNameException;
}
