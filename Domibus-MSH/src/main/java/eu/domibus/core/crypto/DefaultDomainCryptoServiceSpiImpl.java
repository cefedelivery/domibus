package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.spi.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.DomibusCertificateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Qualifier(AbstractCryptoServiceSpi.DEFAULT_IAM_SPI)
public class DefaultDomainCryptoServiceSpiImpl extends Merlin implements DomainCryptoServiceSpi {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultDomainCryptoServiceSpiImpl.class);

    protected Domain domain;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    protected SignalService signalService;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    public void init() {
        LOG.debug("Initializing the certificate provider");

        final Properties allProperties = new Properties();
        allProperties.putAll(getKeystoreProperties());
        allProperties.putAll(getTrustStoreProperties());
        try {
            super.loadProperties(allProperties, Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error loading properties", e);
        }
    }

    @Override
    public X509Certificate getCertificateFromKeyStore(String alias) throws KeyStoreException {
        return (X509Certificate) getKeyStore().getCertificate(alias);
    }

    @Override
    public X509Certificate getCertificateFromTrustStore(String alias) throws KeyStoreException {
        return (X509Certificate) getTrustStore().getCertificate(alias);
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        return domibusPropertyProvider.getProperty(domain, "domibus.security.key.private.password");
    }

    @Override
    public synchronized void refreshTrustStore() {
        final KeyStore trustStore = loadTrustStore();
        setTrustStore(trustStore);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
    public synchronized void replaceTrustStore(byte[] store, String password) throws CryptoSpiException {
        LOG.debug("Replacing the existing trust store file [{}] with the provided one", getTrustStoreLocation());

        ByteArrayOutputStream oldTrustStoreBytes = new ByteArrayOutputStream();
        try {
            truststore.store(oldTrustStoreBytes, getTrustStorePassword().toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException exc) {
            closeOutputStream(oldTrustStoreBytes);
            throw new CryptoSpiException("Could not replace truststore", exc);
        }
        try (ByteArrayInputStream newTrustStoreBytes = new ByteArrayInputStream(store)) {
            certificateService.validateLoadOperation(newTrustStoreBytes, password, getTrustStoreType());
            truststore.load(newTrustStoreBytes, password.toCharArray());
            LOG.debug("Truststore successfully loaded");
            persistTrustStore();
            LOG.debug("Truststore successfully persisted");
        } catch (CertificateException | NoSuchAlgorithmException | IOException | CryptoException e) {
            LOG.error("Could not replace truststore", e);
            try {
                truststore.load(oldTrustStoreBytes.toInputStream(), getTrustStorePassword().toCharArray());
                signalTrustStoreUpdate();
            } catch (CertificateException | NoSuchAlgorithmException | IOException exc) {
                throw new CryptoSpiException("Could not replace truststore and old truststore was not reverted properly. Please correct the error before continuing.", exc);
            }
            throw new CryptoSpiException(e.getMessage(), e);
        } finally {
            closeOutputStream(oldTrustStoreBytes);
        }
    }

    private void closeOutputStream(ByteArrayOutputStream outputStream) {
        try {
            LOG.debug("Closing output stream [{}].", outputStream);
            outputStream.close();
        } catch (IOException e) {
            LOG.error("Could not close [{}]", outputStream, e);
        }
    }

    private synchronized void persistTrustStore() throws CryptoException {
        String trustStoreFileValue = getTrustStoreLocation();
        LOG.debug("TrustoreLocation is: [{}]", trustStoreFileValue);
        File trustStoreFile = new File(trustStoreFileValue);
        if (!trustStoreFile.getParentFile().exists()) {
            LOG.debug("Creating directory [" + trustStoreFile.getParentFile() + "]");
            try {
                FileUtils.forceMkdir(trustStoreFile.getParentFile());
            } catch (IOException e) {
                throw new CryptoException("Could not create parent directory for truststore", e);
            }
        }

        LOG.debug("TrustStoreFile is: [{}]", trustStoreFile.getAbsolutePath());
        try (FileOutputStream fileOutputStream = new FileOutputStream(trustStoreFile)) {
            truststore.store(fileOutputStream, getTrustStorePassword().toCharArray());
        } catch (NoSuchAlgorithmException | IOException | CertificateException | KeyStoreException e) {
            throw new CryptoException("Could not persist truststore:", e);
        }

        signalTrustStoreUpdate();
    }

    @Override
    @Transactional(noRollbackFor = DomibusCertificateException.class)
    public boolean isCertificateChainValid(String alias) throws DomibusCertificateSpiException {
        LOG.debug("Checking certificate validation for [{}]", alias);
        KeyStore trustStore = getTrustStore();
        return certificateService.isCertificateChainValid(trustStore, alias);
    }

    @Override
    public synchronized boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite) {
        boolean added = doAddCertificate(certificate, alias, overwrite);
        persistTrustStore();
        return added;
    }

    @Override
    public synchronized void addCertificate(List<CertificateEntrySpi> certificates, boolean overwrite) {
        certificates.forEach(certEntry ->
                doAddCertificate(certEntry.getCertificate(), certEntry.getAlias(), overwrite));
        persistTrustStore();
    }

    private boolean doAddCertificate(X509Certificate certificate, String alias, boolean overwrite) {
        boolean containsAlias;
        try {
            containsAlias = getTrustStore().containsAlias(alias);
        } catch (final KeyStoreException e) {
            throw new CryptoException("Error while trying to get the alias from the truststore. This should never happen", e);
        }
        if (containsAlias && !overwrite) {
            return false;
        }
        try {
            if (containsAlias) {
                getTrustStore().deleteEntry(alias);
            }
            getTrustStore().setCertificateEntry(alias, certificate);

            return true;
        } catch (final KeyStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    protected KeyStore loadTrustStore() {
        String trustStoreLocation = getTrustStoreLocation();
        if (trustStoreLocation != null) {
            trustStoreLocation = trustStoreLocation.trim();

            try (InputStream is = loadInputStream(this.getClass().getClassLoader(), trustStoreLocation)) {
                String passwd = getTrustStorePassword();
                if (passwd != null) {
                    passwd = passwd.trim();
                    passwd = decryptPassword(passwd, passwordEncryptor);
                }
                String type = getTrustStoreType();
                if (type != null) {
                    type = type.trim();
                }
                final KeyStore trustStore = load(is, passwd, null, type);
                LOG.debug("The TrustStore {} of type {} has been loaded", trustStoreLocation, type);
                return trustStore;
            } catch (WSSecurityException | IOException e) {
                throw new CryptoException("Error loading truststore", e);
            }
        }
        throw new CryptoException("Could not load truststore, truststore location is empty");
    }

    protected Properties getKeystoreProperties() {
        final String keystoreType = domibusPropertyProvider.getProperty(domain, "domibus.security.keystore.type");
        final String keystorePassword = domibusPropertyProvider.getProperty(domain, "domibus.security.keystore.password");
        final String privateKeyAlias = domibusPropertyProvider.getProperty(domain, "domibus.security.key.private.alias");
        final String keystoreLocation = domibusPropertyProvider.getResolvedProperty(domain, "domibus.security.keystore.location");

        Properties result = new Properties();
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, keystoreType);
        final String keyStorePasswordProperty = Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD;
        result.setProperty(keyStorePasswordProperty, keystorePassword);
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, privateKeyAlias);
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_FILE, keystoreLocation);

        Properties logProperties = new Properties();
        logProperties.putAll(result);
        logProperties.remove(keyStorePasswordProperty);
        LOG.debug("Keystore properties for domain [{}] are [{}]", domain, logProperties);

        return result;
    }

    protected Properties getTrustStoreProperties() {
        final String trustStoreType = getTrustStoreType();
        final String trustStorePassword = getTrustStorePassword();
        final String trustStoreLocation = getTrustStoreLocation();

        Properties result = new Properties();
        result.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_TYPE, trustStoreType);
        final String trustStorePasswordProperty = Merlin.PREFIX + Merlin.TRUSTSTORE_PASSWORD;
        result.setProperty(trustStorePasswordProperty, trustStorePassword);
        result.setProperty(Merlin.PREFIX + Merlin.LOAD_CA_CERTS, "false");
        result.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_FILE, trustStoreLocation);

        Properties logProperties = new Properties();
        logProperties.putAll(result);
        logProperties.remove(trustStorePasswordProperty);
        LOG.debug("Truststore properties for domain [{}] are [{}]", domain, logProperties);

        return result;
    }

    protected String getTrustStoreLocation() {
        return domibusPropertyProvider.getResolvedProperty(domain, "domibus.security.truststore.location");
    }

    protected String getTrustStorePassword() {
        return domibusPropertyProvider.getProperty(domain, "domibus.security.truststore.password");
    }

    protected String getTrustStoreType() {
        return domibusPropertyProvider.getProperty(domain, "domibus.security.truststore.type");
    }

    protected void signalTrustStoreUpdate() {
        // Sends a signal to all the servers from the cluster in order to trigger the refresh of the trust store
        signalService.signalTrustStoreUpdate(domain);
    }

    @Override
    public boolean removeCertificate(String alias) {
        boolean removed = doRemoveCertificate(alias);
        persistTrustStore();
        return removed;
    }

    @Override
    public void removeCertificate(List<String> aliases) {
        aliases.forEach(this::doRemoveCertificate);
        persistTrustStore();
    }

    @Override
    public String getIdentifier() {
        return AbstractCryptoServiceSpi.DEFAULT_IAM_SPI;
    }

    @Override
    public void setDomain(DomainSpi domain) {
        this.domain = domainCoreConverter.convert(domain, Domain.class);
    }

    private synchronized boolean doRemoveCertificate(String alias) {
        boolean containsAlias;
        try {
            containsAlias = getTrustStore().containsAlias(alias);
        } catch (final KeyStoreException e) {
            throw new CryptoException("Error while trying to get the alias from the truststore. This should never happen", e);
        }
        if (!containsAlias) {
            return false;
        }
        try {
            getTrustStore().deleteEntry(alias);
            return true;
        } catch (final KeyStoreException e) {
            throw new ConfigurationException(e);
        }
    }
}
