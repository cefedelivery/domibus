package eu.domibus.core.crypto.spi;

import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Cosmin Baciu
 * @see org.apache.wss4j.common.crypto.CryptoBase
 * @since 4.0
 * <p>
 * Same methods as CryptoBase defined in the IAM api. Will be used as a delegate by core DomainCryptoService
 */
public interface DomainCryptoServiceSpi {
    /* START - Methods required to be implemented by the org.apache.wss4j.common.crypto.CryptoBase */
    X509Certificate[] getX509Certificates(CryptoType cryptoType) throws WSSecurityException;

    String getX509Identifier(X509Certificate cert) throws WSSecurityException;

    PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException;

    PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException;

    PrivateKey getPrivateKey(String identifier, String password) throws WSSecurityException;

    void verifyTrust(PublicKey publicKey) throws WSSecurityException;

    void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException;

    String getDefaultX509Identifier() throws WSSecurityException;
    /* END - Methods required to be implemented by the org.apache.wss4j.common.crypto.CryptoBase */

    String getPrivateKeyPassword(String alias);

    void refreshTrustStore();

    void replaceTrustStore(byte[] store, String password) throws CryptoSpiException;

    KeyStore getKeyStore();

    KeyStore getTrustStore();

    X509Certificate getCertificateFromKeyStore(String alias) throws KeyStoreException;

    boolean isCertificateChainValid(String alias) throws DomibusCertificateSpiException;

    boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite);

    void addCertificate(List<CertificateEntrySpi> certificates, boolean overwrite);

    X509Certificate getCertificateFromTrustStore(String alias) throws KeyStoreException;

    boolean removeCertificate(String alias);

    void removeCertificate(List<String> aliases);

    String getIdentifier();

    void setDomain(DomainSpi domain);

    void init();
}
