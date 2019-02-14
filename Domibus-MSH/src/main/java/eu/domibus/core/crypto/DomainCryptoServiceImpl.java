package eu.domibus.core.crypto;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.CertificateEntry;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.crypto.spi.DomibusCertificateException;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.security.auth.callback.CallbackHandler;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class DomainCryptoServiceImpl implements DomainCryptoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCryptoServiceImpl.class);

    private DomainCryptoServiceSpi iamProvider;

    private Domain domain;

    @Autowired
    private List<DomainCryptoServiceSpi> domainCryptoServiceSpiList;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    protected static final String PROPERTY_NAME = "domibus.iam.extension.identifier";

    public DomainCryptoServiceImpl() {
    }

    public DomainCryptoServiceImpl(Domain domain) {
        this.domain = domain;
    }

    @PostConstruct
    public void init() {
        String spiIdentifier = domibusPropertyProvider.getDomainProperty(PROPERTY_NAME);
        final List<DomainCryptoServiceSpi> providerList = domainCryptoServiceSpiList.stream().
                filter(domainCryptoServiceSpi -> spiIdentifier.equals(domainCryptoServiceSpi.getIdentifier())).collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("IAM spi:");
            providerList.stream().forEach(domainCryptoServiceSpi -> LOG.debug(" identifier:[{}] for class:[{}]", domainCryptoServiceSpi.getIdentifier(), domainCryptoServiceSpi.getClass()));
        }

        if (providerList.size() > 1) {
            LOG.error("More than one IAM service provider for identifier:[{}]", spiIdentifier);
            throw new IllegalStateException("More than one IAM service provider for given identifier");
        }
        if (providerList.isEmpty()) {
            LOG.error("No IAM service provider found for given identifier:[{}]", spiIdentifier);
            throw new IllegalStateException("No IAM service provider found for given identifier");
        }

        iamProvider = providerList.get(0);
        iamProvider.setDomain(new eu.domibus.core.crypto.spi.Domain(domain.getCode(), domain.getName()));
        iamProvider.init();

        LOG.info("Active IAM provider identifier:[{}] for domain:[{}]", iamProvider.getIdentifier(), domain.getName());
    }


    private DomainCryptoServiceSpi getIamProvider() {
        return iamProvider;
    }

    @Override
    public X509Certificate getCertificateFromKeyStore(String alias) throws KeyStoreException {
        return getIamProvider().getCertificateFromKeyStore(alias);
    }

    @Override
    public X509Certificate getCertificateFromTrustStore(String alias) throws KeyStoreException {
        return getIamProvider().getCertificateFromTrustStore(alias);
    }

    @Override
    public X509Certificate[] getX509Certificates(CryptoType cryptoType) throws WSSecurityException {
        return getIamProvider().getX509Certificates(cryptoType);
    }

    @Override
    public String getX509Identifier(X509Certificate cert) throws WSSecurityException {
        return getIamProvider().getX509Identifier(cert);
    }

    @Override
    public PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException {
        return getIamProvider().getPrivateKey(certificate, callbackHandler);
    }

    @Override
    public PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException {
        return getIamProvider().getPrivateKey(publicKey, callbackHandler);
    }

    @Override
    public PrivateKey getPrivateKey(String identifier, String password) throws WSSecurityException {
        return getIamProvider().getPrivateKey(identifier, password);
    }

    @Override
    public void verifyTrust(PublicKey publicKey) throws WSSecurityException {
        getIamProvider().verifyTrust(publicKey);
    }

    @Override
    public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {
        getIamProvider().verifyTrust(certs, enableRevocation, subjectCertConstraints, issuerCertConstraints);
    }

    @Override
    public String getDefaultX509Identifier() throws WSSecurityException {
        return getIamProvider().getDefaultX509Identifier();
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        return getIamProvider().getPrivateKeyPassword(alias);
    }

    @Override
    public synchronized void refreshTrustStore() throws CryptoException {
        getIamProvider().refreshTrustStore();
    }

    @Override
    public synchronized void replaceTrustStore(byte[] store, String password) throws CryptoException {
        getIamProvider().replaceTrustStore(store, password);
    }

    @Override
    public KeyStore getKeyStore() {
        return getIamProvider().getKeyStore();
    }

    @Override
    public KeyStore getTrustStore() {
        return getIamProvider().getTrustStore();
    }


    @Override
    public boolean isCertificateChainValid(String alias) throws DomibusCertificateException {
        return getIamProvider().isCertificateChainValid(alias);
    }

    @Override
    public synchronized boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite) {
        return getIamProvider().addCertificate(certificate, alias, overwrite);
    }

    @Override
    public synchronized void addCertificate(List<CertificateEntry> certificates, boolean overwrite) {
        getIamProvider().addCertificate(domainCoreConverter.convert(certificates, eu.domibus.core.crypto.spi.CertificateEntry.class), overwrite);
    }

    @Override
    public boolean removeCertificate(String alias) {
        return getIamProvider().removeCertificate(alias);
    }

    @Override
    public void removeCertificate(List<String> aliases) {
        getIamProvider().removeCertificate(aliases);
    }

    protected void setDomainCryptoServiceSpiList(List<DomainCryptoServiceSpi> domainCryptoServiceSpiList) {
        this.domainCryptoServiceSpiList = domainCryptoServiceSpiList;
    }
}
