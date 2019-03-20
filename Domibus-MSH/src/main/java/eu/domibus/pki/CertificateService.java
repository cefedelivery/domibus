package eu.domibus.pki;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.TrustStoreEntry;

import javax.naming.InvalidNameException;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
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

    /**
     * Validates that the bytes represent a valid truststore
     * @param newTrustStoreBytes the content
     * @param password the password to open the truststore
     * @param type the type of the truststore: jks, PKCS12
     */
    void validateLoadOperation(ByteArrayInputStream newTrustStoreBytes, String password, String type);

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
     * Given a list of certificates, returns a string containing the certificates in a 64 base encoded format and
     * separated in the Pem style.
     *
     * @param certificates the list of certificates.
     * @return the pem formatted string.
     */
    String serializeCertificateChainIntoPemFormat(List<? extends Certificate> certificates);

    /**
     * Given a pem formatted string containing a list of certificates, the method returns a list of X509 certificates.
     *
     * @param chain the pem formatted string.
     * @return the list of certificates.
     */
    List<X509Certificate> deserializeCertificateChainFromPemFormat(String chain);

    /**
     * Given a chain of signing certificates (Trust chain + leaf), extract the leaf one.
     *
     * @param certificates list containing the trust chain and the leaf.
     * @return the leaf certificate.
     */
    Certificate extractLeafCertificateFromChain(List<? extends Certificate> certificates);

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
