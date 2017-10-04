package eu.domibus.pki;

import eu.domibus.api.security.TrustStoreEntry;

import javax.naming.InvalidNameException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @Author Cosmin Baciu
 * @Since 3.2
 */
public interface CertificateService {

    boolean isCertificateValid(X509Certificate cert) throws DomibusCertificateException;

    boolean isCertificateChainValid(String alias) throws DomibusCertificateException;

    boolean isCertificateValid(String alias) throws DomibusCertificateException;

    String extractCommonName(final X509Certificate certificate) throws InvalidNameException;

    X509Certificate loadCertificateFromJKSFile(String filePath, String alias, String password);

    /**
     * Returne the detail of the truststore entries.
     *
     * @return a list of certificate
     */
    List<TrustStoreEntry> getTrustStoreEntries();
}
