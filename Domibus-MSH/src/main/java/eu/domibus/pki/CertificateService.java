package eu.domibus.pki;

import java.security.cert.X509Certificate;

/**
 * Created by Cosmin Baciu on 12-Jul-16.
 */
public interface CertificateService {

    boolean isCertificateValidationEnabled();

    boolean isCertificateValid(X509Certificate cert) throws DomibusCertificateException;

    boolean isCertificateChainValid(String alias) throws DomibusCertificateException;

    boolean isCertificateValid(String alias) throws DomibusCertificateException;
}
