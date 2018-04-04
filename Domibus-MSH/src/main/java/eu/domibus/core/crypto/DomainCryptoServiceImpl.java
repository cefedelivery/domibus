package eu.domibus.core.crypto;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.clustering.Command;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.DomibusCertificateException;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.core.crypto.api.DomainPropertyProvider;
import org.apache.commons.io.FileUtils;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

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
    DomainPropertyProvider domainPropertyProvider;

    @Autowired
    CertificateService certificateService;

    @Qualifier("jmsTemplateCommand")
    @Autowired
    private JmsOperations jmsOperations;

    @PostConstruct
    public void init() {
        LOG.debug("Initializing the certificate provider");

        final Properties allProperties = new Properties();
        allProperties.putAll(getKeystoreProperties(domain));
        allProperties.putAll(getTrustStoreProperties(domain));
        try {
            super.loadProperties(allProperties, Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CryptoException(DomibusCoreErrorCode.DOM_001, "Error loading properties", e);
        }
    }

    @Override
    public X509Certificate getCertificateFromKeystore(String alias) throws KeyStoreException {
        return (X509Certificate) getKeyStore().getCertificate(alias);
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        return domainPropertyProvider.getPropertyValue(domain, "domibus.security.key.private.password");
    }

    @Override
    public synchronized void refreshTrustStore() throws CryptoException {
        final KeyStore trustStore = loadTrustStore();
        setTrustStore(trustStore);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public synchronized void replaceTrustStore(byte[] store, String password) throws CryptoException {
        String trustStoreFileValue = getTrustStoreLocation();
        File trustStoreFile = new File(trustStoreFileValue);
        if (!trustStoreFile.getParentFile().exists()) {
            LOG.debug("Creating directory [" + trustStoreFile.getParentFile() + "]");
            try {
                FileUtils.forceMkdir(trustStoreFile.getParentFile());
            } catch (IOException e) {
                throw new CryptoException("Could not create parent directory for truststore", e);
            }
        }

        LOG.debug("Replacing the existing trust store file [" + trustStoreFileValue + "] with the provided one");
        try (ByteArrayInputStream newTrustStoreBytes = new ByteArrayInputStream(store)) {
            certificateService.validateLoadOperation(newTrustStoreBytes, password);

            truststore.load(newTrustStoreBytes, password.toCharArray());
            try (FileOutputStream fileOutputStream = new FileOutputStream(trustStoreFile)) {
                truststore.store(fileOutputStream, getTrustStorePassword().toCharArray());
            }
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new CryptoException("Could not replace truststore", e);
        }

        signalTrustStoreUpdate();
    }

    @Override
    @Transactional(noRollbackFor = DomibusCertificateException.class)
    public boolean isCertificateChainValid(String alias) throws DomibusCertificateException {
        LOG.debug("Checking certificate validation for [" + alias + "]");
        KeyStore trustStore = getTrustStore();
        return certificateService.isCertificateChainValid(trustStore, alias);
    }

    @Override
    public synchronized boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite) {
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

    protected Properties getKeystoreProperties(Domain domain) {
        final String keystoreType = domainPropertyProvider.getPropertyValue(domain, "domibus.security.keystore.type");
        final String keystorePassword = domainPropertyProvider.getPropertyValue(domain, "domibus.security.keystore.password");
        final String privateKeyAlias = domainPropertyProvider.getPropertyValue(domain, "domibus.security.key.private.alias");
        final String keystoreLocation = domainPropertyProvider.getResolvedPropertyValue(domain, "domibus.security.keystore.location");

        Properties result = new Properties();
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, keystoreType);
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD, keystorePassword);
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, privateKeyAlias);
        result.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_FILE, keystoreLocation);

        LOG.debug("Keystore properties for domain [{}] are [{}]", domain, result);

        return result;
    }

    protected Properties getTrustStoreProperties(Domain domain) {
        final String trustStoreType = getTrustStoreType();
        final String trustStorePassword = getTrustStorePassword();
        final String trustStoreLocation = getTrustStoreLocation();

        Properties result = new Properties();
        result.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_TYPE, trustStoreType);
        result.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_PASSWORD, trustStorePassword);
        result.setProperty(Merlin.PREFIX + Merlin.LOAD_CA_CERTS, "false");
        result.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_FILE, trustStoreLocation);

        LOG.debug("Truststore properties for domain [{}] are [{}]", domain, result);

        return result;
    }

    protected String getTrustStoreLocation() {
        return domainPropertyProvider.getResolvedPropertyValue(domain, "domibus.security.truststore.location");
    }

    protected String getTrustStorePassword() {
        return domainPropertyProvider.getPropertyValue(domain, "domibus.security.truststore.password");
    }

    protected String getTrustStoreType() {
        return domainPropertyProvider.getPropertyValue(domain, "domibus.security.truststore.type");
    }

    protected void signalTrustStoreUpdate() {
        // Sends a signal to all the servers from the cluster in order to trigger the refresh of the trust store
        jmsOperations.send(new ReloadTrustStoreMessageCreator());
    }

    class ReloadTrustStoreMessageCreator implements MessageCreator {
        @Override
        public Message createMessage(Session session) throws JMSException {
            Message m = session.createMessage();
            m.setStringProperty(Command.COMMAND, Command.RELOAD_TRUSTSTORE);
            m.setStringProperty("domain", domain.getCode());
            return m;
        }
    }
}
