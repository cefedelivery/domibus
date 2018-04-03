package eu.domibus.wss4j.common.crypto.api;

import eu.domibus.pki.DomibusCertificateException;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainCertificateProvider {

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

    void replaceTrustStore(byte[] store, String password) throws CryptoException;

    KeyStore getKeyStore();

    KeyStore getTrustStore();

    X509Certificate getCertificateFromKeystore(String alias) throws KeyStoreException;

    boolean isCertificateChainValid(String alias) throws DomibusCertificateException;

    boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite);
}
