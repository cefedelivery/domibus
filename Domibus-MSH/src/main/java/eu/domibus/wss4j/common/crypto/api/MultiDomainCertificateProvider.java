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
public interface MultiDomainCertificateProvider {

    X509Certificate[] getX509Certificates(String domain, CryptoType cryptoType) throws WSSecurityException;

    String getX509Identifier(String domain, X509Certificate cert) throws WSSecurityException;

    PrivateKey getPrivateKey(String domain, X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException;

    PrivateKey getPrivateKey(String domain, PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException;

    PrivateKey getPrivateKey(String domain, String identifier, String password) throws WSSecurityException;

    void verifyTrust(String domain, X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException;

    void verifyTrust(String domain, PublicKey publicKey) throws WSSecurityException;

    String getDefaultX509Identifier(String domain) throws WSSecurityException;

    String getPrivateKeyPassword(String domain, String privateKeyAlias);

    void refreshTrustStore(String domain);

    void replaceTrustStore(String domain, byte[] store, String password) throws CryptoException;

    KeyStore getKeyStore(String domain);

    KeyStore getTrustStore(String domain);

    boolean isCertificateChainValid(String domain, String alias) throws DomibusCertificateException;

    X509Certificate getCertificateFromKeystore(String currentDomain, String senderName) throws KeyStoreException;

    boolean addCertificate(String domain, final X509Certificate certificate, final String alias, final boolean overwrite);
}
