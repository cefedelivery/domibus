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
public interface MultiDomainCertificateProvider {

    X509Certificate[] getX509Certificates(String domain, CryptoType cryptoType) throws WSSecurityException;

    String getX509Identifier(String currentDomain, X509Certificate cert) throws WSSecurityException;

    PrivateKey getPrivateKey(String currentDomain, X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException;

    PrivateKey getPrivateKey(String currentDomain, PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException;

    PrivateKey getPrivateKey(String currentDomain, String identifier, String password) throws WSSecurityException;

    void verifyTrust(String currentDomain, X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException;

    void verifyTrust(String currentDomain, PublicKey publicKey) throws WSSecurityException;

    String getDefaultX509Identifier(String currentDomain) throws WSSecurityException;
}
