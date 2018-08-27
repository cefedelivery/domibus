package eu.domibus.pki;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.TrustStoreEntry;

import javax.naming.InvalidNameException;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
public interface CertificateService {

    boolean isCertificateValid(X509Certificate cert) throws DomibusCertificateException;

    boolean isCertificateChainValid(KeyStore trustStore, String alias);

    String extractCommonName(final X509Certificate certificate) throws InvalidNameException;

    X509Certificate loadCertificateFromJKSFile(String filePath, String alias, String password);

    /**
     * Return the detail of the truststore entries.
     *
     * @param trustStore the trust store from where to retrieve the certificates
     *
     * @return a list of certificate
     */
    List<TrustStoreEntry> getTrustStoreEntries(final KeyStore trustStore);

    /**
     * Save certificate data in the database, and use this data to display a revocation warning when needed.
     * @param domain the current domain
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
     * @param content the certificate serialized as a base64 string
     *
     * @return a certificate
     * @throws CertificateException if the base64 string cannot be deserialized to a certificate
     */
    X509Certificate loadCertificateFromString(String content) throws CertificateException;

    /**
     * Returns the certificate entry from the trust store given an alias
     *
     * @param alias the certificate alias
     *
     * @return a certificate entry
     * @throws KeyStoreException if the trust store was not initialized
     */
    TrustStoreEntry getPartyCertificateFromTruststore(String alias) throws KeyStoreException;

    /**
     * Returns a certificate entry converted from a base64 string
     *
     * @param certificateContent the certificate serialized as a base64 string
     *
     * @return a certificate entry
     * @throws CertificateException if the base64 string cannot be converted to a certificate entry
     */
    TrustStoreEntry convertCertificateContent(String certificateContent) throws CertificateException;
}
