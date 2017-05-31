package eu.domibus.pki;

import javax.naming.InvalidNameException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Created by Cosmin Baciu on 12-Jul-16.
 */
public interface CertificateService {

    boolean isCertificateValidationEnabled();

    boolean isCertificateValid(X509Certificate cert) throws DomibusCertificateException;

    boolean isCertificateChainValid(String alias) throws DomibusCertificateException;

    String extractCommonName(final X509Certificate certificate) throws InvalidNameException;

    X509Certificate loadCertificateFromJKSFile(String filePath, String alias, String password);
}
