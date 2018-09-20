package eu.domibus.core.crypto;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.clustering.Command;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.model.certificate.Certificate;
import eu.domibus.core.crypto.api.CertificateEntry;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.DomibusCertificateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.jms.Topic;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class DomainCryptoServiceImpl extends Merlin implements DomainCryptoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCryptoServiceImpl.class);

    protected Domain domain;

    public DomainCryptoServiceImpl() {
    }

    public DomainCryptoServiceImpl(Domain domain) {
        this.domain = domain;
    }

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    protected JMSManager jmsManager;

    @Autowired
    protected Topic clusterCommandTopic;

    @PostConstruct
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
    public synchronized void refreshTrustStore() throws CryptoException {
        final KeyStore trustStore = loadTrustStore();
        setTrustStore(trustStore);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
    public synchronized void replaceTrustStore(byte[] store, String password) throws CryptoException {
        LOG.debug("Replacing the existing trust store file [{}] with the provided one", getTrustStoreLocation());

        ByteArrayOutputStream oldTrustStoreBytes = new ByteArrayOutputStream();
        try {
            truststore.store(oldTrustStoreBytes, getTrustStorePassword().toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException exc) {
            throw new CryptoException("Could not replace truststore", exc);
        }
        try (ByteArrayInputStream newTrustStoreBytes = new ByteArrayInputStream(store)) {
            certificateService.validateLoadOperation(newTrustStoreBytes, password);
            truststore.load(newTrustStoreBytes, password.toCharArray());
            LOG.debug("Truststore successfully loaded");
            persistTrustStore();
            LOG.debug("Truststore successfully persisted");
        } catch (CertificateException | NoSuchAlgorithmException | IOException | CryptoException e) {
            LOG.error("Could not replace truststore", e);
            try {
                truststore.load(oldTrustStoreBytes.toInputStream(), getTrustStorePassword().toCharArray());
            } catch (CertificateException | NoSuchAlgorithmException | IOException exc) {
                throw new CryptoException("Could not replace truststore and old truststore was not reverted properly. Please correct the error before continuing.", exc);
            }
            throw new CryptoException("Could not replace truststore, using old truststore", e);
        } finally {
            try {
                LOG.debug("Closing old truststore output stream.");
                oldTrustStoreBytes.close();
            } catch (IOException e) {
                LOG.error("Could not close oldTrustStoreBytes output stream", e);
            }
        }

        signalTrustStoreUpdate();
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
    public boolean isCertificateChainValid(String alias) throws DomibusCertificateException {
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
    public synchronized boolean addCertificate(List<CertificateEntry> certificates, boolean overwrite) {
        Boolean[] added = new Boolean[certificates.size()];
        for (int i = 0; i < certificates.size(); i++) {
            String alias = certificates.get(i).getAlias();
            X509Certificate cert = certificates.get(i).getCertificate();
            added[i] = doAddCertificate(cert, alias, overwrite);
        }
        persistTrustStore();
        return Stream.of(added).allMatch(el -> el.equals(true));
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
        jmsManager.sendMessageToTopic(JMSMessageBuilder.create()
                .property(Command.COMMAND, Command.RELOAD_TRUSTSTORE)
                .property(MessageConstants.DOMAIN, domain.getCode())
                .build(), clusterCommandTopic);
    }

    @Override
    public boolean removeCertificate(String alias) {
        boolean removed = doRemoveCertificate(alias);
        persistTrustStore();
        return removed;
    }

    @Override
    public boolean removeCertificate(List<String> aliases) {
        Boolean[] removed = new Boolean[aliases.size()];
        for (int i = 0; i < aliases.size(); i++) {
            removed[i] = removeCertificate(aliases.get(i));
        }
        persistTrustStore();
        return Stream.of(removed).allMatch(el -> el.equals(true));
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
