package eu.domibus.wss4j.common.crypto;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.DomibusCertificateException;
import eu.domibus.wss4j.common.crypto.api.CryptoException;
import eu.domibus.wss4j.common.crypto.api.DomainCertificateProvider;
import eu.domibus.wss4j.common.crypto.api.DomainCertificateProviderFactory;
import eu.domibus.wss4j.common.crypto.api.MultiDomainCertificateProvider;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class MultiDomainCertificateProviderImpl implements MultiDomainCertificateProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiDomainCertificateProviderImpl.class);

    protected volatile Map<String, DomainCertificateProvider> domainCertificateProviderMap = new HashMap<>();

    @Autowired
    DomainCertificateProviderFactory domainCertificateProviderFactory;

    @Override
    public X509Certificate[] getX509Certificates(String domain, CryptoType cryptoType) throws WSSecurityException {
        LOG.debug("Get certificates for domain [{}] and cryptoType [{}]", domain, cryptoType);
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getX509Certificates(cryptoType);
    }

    protected DomainCertificateProvider getDomainCertificateProvider(String domain)  {
        LOG.debug("Get domain CertificateProvider for domain [{}]", domain);
        DomainCertificateProvider domainCertificateProvider = domainCertificateProviderMap.get(domain);
        if (domainCertificateProvider == null) {
            synchronized (domainCertificateProviderMap) {
                if (domainCertificateProvider == null) {
                    LOG.debug("Creating domain CertificateProvider for domain [{}]", domain);
                    domainCertificateProvider = domainCertificateProviderFactory.createDomainCertificateProvider(domain);
                    domainCertificateProviderMap.put(domain, domainCertificateProvider);
                }
            }
        }
        return domainCertificateProvider;
    }

    @Override
    public String getX509Identifier(String domain, X509Certificate cert) throws WSSecurityException {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getX509Identifier(cert);
    }

    @Override
    public PrivateKey getPrivateKey(String domain, X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getPrivateKey(certificate, callbackHandler);
    }

    @Override
    public PrivateKey getPrivateKey(String domain, PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getPrivateKey(publicKey, callbackHandler);
    }

    @Override
    public PrivateKey getPrivateKey(String domain, String identifier, String password) throws WSSecurityException {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getPrivateKey(identifier, password);
    }

    @Override
    public void verifyTrust(String domain, X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.verifyTrust(certs, enableRevocation, subjectCertConstraints, issuerCertConstraints);
    }

    @Override
    public void verifyTrust(String domain, PublicKey publicKey) throws WSSecurityException {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain) ;
        domainCertificateProvider.verifyTrust(publicKey);
    }

    @Override
    public String getDefaultX509Identifier(String domain) throws WSSecurityException {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getDefaultX509Identifier();
    }

    @Override
    public String getPrivateKeyPassword(String domain, String privateKeyAlias) {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getPrivateKeyPassword(privateKeyAlias);
    }

    @Override
    public void refreshTrustStore(String domain) {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.refreshTrustStore();
    }

    @Override
    public void replaceTrustStore(String domain, byte[] store, String password) throws CryptoException {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        domainCertificateProvider.replaceTrustStore(store, password);
    }

    @Override
    public KeyStore getKeyStore(String domain) {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getKeyStore();
    }

    @Override
    public KeyStore getTrustStore(String domain) {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getTrustStore();
    }

    @Override
    @Transactional(noRollbackFor = DomibusCertificateException.class)
    @Cacheable(value = "certValidationByAlias", key = "#domain + #alias")
    public boolean isCertificateChainValid(String domain, String alias) throws DomibusCertificateException {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.isCertificateChainValid(alias);
    }

    @Override
    public X509Certificate getCertificateFromKeystore(String domain, String alias) throws KeyStoreException {
        final DomainCertificateProvider domainCertificateProvider = getDomainCertificateProvider(domain);
        return domainCertificateProvider.getCertificateFromKeystore(alias);
    }
}
