package eu.domibus.wss4j.common.crypto.api;

import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;

import javax.security.auth.callback.CallbackHandler;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author baciu
 * @since 3.3
 */
public interface DomainCertificateProvider {

    X509Certificate[] getX509Certificates(CryptoType cryptoType) throws WSSecurityException;

    String getX509Identifier(X509Certificate cert) throws WSSecurityException;

    PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException;

    PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException;

    PrivateKey getPrivateKey(String identifier, String password) throws WSSecurityException;

    void verifyTrust(PublicKey publicKey) throws WSSecurityException;

    void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException;

    String getDefaultX509Identifier() throws WSSecurityException;

    String getPrivateKeyPassword(String alias);

}
