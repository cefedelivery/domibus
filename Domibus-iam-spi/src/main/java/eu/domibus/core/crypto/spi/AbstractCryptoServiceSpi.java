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
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Abstraction wrapper around the default CryptorService that use Merlin within CXF.
 * Any custom authentication implementation should extend this class and override needed method.
 */
public abstract class AbstractCryptoServiceSpi implements DomainCryptoServiceSpi {

    public static final String DEFAULT_AUTHENTICATION_SPI = "DEFAULT_AUTHENTICATION_SPI";

    private DomainCryptoServiceSpi defaultDomainCryptoService;

    private DomainSpi domain;

    public AbstractCryptoServiceSpi(DomainCryptoServiceSpi defaultDomainCryptoService) {
        this.defaultDomainCryptoService = defaultDomainCryptoService;
    }

    @Override
    public X509Certificate[] getX509Certificates(CryptoType cryptoType) throws WSSecurityException {
        return defaultDomainCryptoService.getX509Certificates(cryptoType);
    }

    @Override
    public String getX509Identifier(X509Certificate cert) throws WSSecurityException {
        return defaultDomainCryptoService.getX509Identifier(cert);
    }

    @Override
    public PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException {
        return defaultDomainCryptoService.getPrivateKey(certificate, callbackHandler);
    }

    @Override
    public PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException {
        return defaultDomainCryptoService.getPrivateKey(publicKey, callbackHandler);
    }

    @Override
    public PrivateKey getPrivateKey(String identifier, String password) throws WSSecurityException {
        return defaultDomainCryptoService.getPrivateKey(identifier, password);
    }

    @Override
    public void verifyTrust(PublicKey publicKey) throws WSSecurityException {
        defaultDomainCryptoService.verifyTrust(publicKey);
    }

    @Override
    public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {
        defaultDomainCryptoService.verifyTrust(certs, enableRevocation, subjectCertConstraints, issuerCertConstraints);
    }

    @Override
    public String getDefaultX509Identifier() throws WSSecurityException {
        return defaultDomainCryptoService.getDefaultX509Identifier();
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        return defaultDomainCryptoService.getPrivateKeyPassword(alias);
    }

    @Override
    public void refreshTrustStore() {
        defaultDomainCryptoService.refreshTrustStore();
    }

    @Override
    public void replaceTrustStore(byte[] store, String password) {
        defaultDomainCryptoService.replaceTrustStore(store, password);
    }

    @Override
    public KeyStore getKeyStore() {
        return defaultDomainCryptoService.getKeyStore();
    }

    @Override
    public KeyStore getTrustStore() {
        return defaultDomainCryptoService.getTrustStore();
    }

    @Override
    public X509Certificate getCertificateFromKeyStore(String alias) throws KeyStoreException {
        return defaultDomainCryptoService.getCertificateFromKeyStore(alias);
    }

    @Override
    public boolean isCertificateChainValid(String alias) throws DomibusCertificateSpiException {
        return defaultDomainCryptoService.isCertificateChainValid(alias);
    }

    @Override
    public boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite) {
        return defaultDomainCryptoService.addCertificate(certificate, alias, overwrite);
    }

    @Override
    public void addCertificate(List<CertificateEntrySpi> certificates, boolean overwrite) {
        defaultDomainCryptoService.addCertificate(certificates, overwrite);
    }

    @Override
    public X509Certificate getCertificateFromTrustStore(String alias) throws KeyStoreException {
        return defaultDomainCryptoService.getCertificateFromTrustStore(alias);
    }

    @Override
    public boolean removeCertificate(String alias) {
        return defaultDomainCryptoService.removeCertificate(alias);
    }

    @Override
    public void removeCertificate(List<String> aliases) {
        defaultDomainCryptoService.removeCertificate(aliases);
    }

    @Override
    public void setDomain(DomainSpi domain) {
        defaultDomainCryptoService.setDomain(domain);
        this.domain = domain;
    }

    protected DomainSpi getDomain() {
        return domain;
    }

    @Override
    public void init() {
        defaultDomainCryptoService.init();
    }


}
