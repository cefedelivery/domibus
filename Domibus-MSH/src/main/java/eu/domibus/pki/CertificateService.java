package eu.domibus.pki;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.TrustStoreEntry;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InvalidNameException;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @Author Cosmin Baciu
 * @Since 3.2
 */
public interface CertificateService {

    boolean isCertificateValid(X509Certificate cert) throws DomibusCertificateException;

    boolean isCertificateChainValid(KeyStore trustStore, String alias);

    String extractCommonName(final X509Certificate certificate) throws InvalidNameException;

    X509Certificate loadCertificateFromJKSFile(String filePath, String alias, String password);

    /**
     * Returne the detail of the truststore entries.
     *
     * @return a list of certificate
     */
    List<TrustStoreEntry> getTrustStoreEntries(final KeyStore trustStore);

    /**
     * Save certificate data in the database, and use this data to display a revocation warning when needed.
     */
    void saveCertificateAndLogRevocation(Domain domain);


    void validateLoadOperation(ByteArrayInputStream newTrustStoreBytes, String password);

    /**
     * Check if alerts need to be send for expired or soon expired certificate. Send if true.
     */
    void sendCertificateAlerts();

    /**
     * Returns the certificate deserialized from a base64 string
     *
     * @return a certificate
     */
    X509Certificate loadCertificateFromString(String content);

    /**
     * Returns the certificate entry from the trust store given an alias
     *
     * @return a certificate entry
     */
    TrustStoreEntry getPartyCertificateFromTruststore(String alias) throws KeyStoreException;

    /**
     * Returns a certificate entry converted from a base64 string
     *
     * @return a certificate entry
     */
    TrustStoreEntry convertCertificateContent(String certificateContent);
}
