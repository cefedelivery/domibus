package eu.domibus.pki;

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

}
