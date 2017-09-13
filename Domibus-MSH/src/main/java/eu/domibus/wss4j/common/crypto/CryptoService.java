package eu.domibus.wss4j.common.crypto;

import eu.domibus.clustering.Command;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 */
@Service(value = "cryptoService")
public class CryptoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CryptoService.class);

    @Resource(name = "trustStoreProperties")
    private Properties trustStoreProperties;

    @Resource(name = "keystoreProperties")
    private Properties keystoreProperties;

    @Qualifier("jmsTemplateCommand")
    @Autowired
    private JmsOperations jmsOperations;

    private KeyStore trustStore;

    private Merlin crypto;

    public synchronized KeyStore getTrustStore() {
        if (trustStore == null) {
            try {
                initTrustStore();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | WSSecurityException e) {
                LOG.error("Error while initializing trustStore", e);
            }
        }
        return trustStore;
    }

    /**
     * Adds the certificate to the trustStore
     *
     * @param certificate the certificate to add
     * @param alias       the certifictae alias
     * @param overwrite   if {@value true} existing entries will be replaced
     * @return {@value true} if added, else {@value false}
     */
    public boolean addCertificate(final X509Certificate certificate, final String alias, final boolean overwrite) {
        boolean containsAlias = false;
        try {
            containsAlias = getTrustStore().containsAlias(alias);
        } catch (final KeyStoreException e) {
            throw new ConfigurationException("Error while trying to get the alias from the truststore. This should never happen", e);
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

    private void initTrustStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, WSSecurityException {

        if (trustStore == null) {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        }
        String trustStoreFilename = trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.file");
        String trustStorePassword = trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.password");
        loadTrustore(trustStoreFilename, trustStorePassword);
        /*trustStore.load(new FileInputStream(trustStoreFilename), trustStorePassword.toCharArray());*/
        /*if(trustStore==null) {
            trustStore = ks;
            return;
        }
        final Enumeration<String> currentCustorEntryKeys = trustStore.aliases();
        while (currentCustorEntryKeys.hasMoreElements()){
            trustStore.deleteEntry(currentCustorEntryKeys.nextElement());
        }
        trustStore.load();
        final Enumeration<String> newTrustoreEntryKeys = ks.aliases();
        while (newTrustoreEntryKeys.hasMoreElements()){
            final String newTrustoreEntryKey = newTrustoreEntryKeys.nextElement();
            trustStore.setCertificateEntry(newTrustoreEntryKey,newTrustoreEntryKeys);
        }
        LOG.info("TrustStore successfully loaded");
        crypto = (Merlin) CryptoFactory.getInstance(trustStoreProperties);
        crypto.setTrustStore(trustStore);*/

    }

    protected void loadTrustore(String trustStoreFilename, String password) throws IOException, CertificateException, NoSuchAlgorithmException {
        trustStore.load(new FileInputStream(trustStoreFilename), password.toCharArray());
    }

    public void refreshTrustStore() {
        try {
            initTrustStore();
            // After startup and before the first message is sent the crypto is not initialized yet, so there is no need to refresh the trustStore in it!
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | WSSecurityException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("Failed to reload certificates due to: " + ex);
            } else {
                LOG.warn("Failed to reload certificates due to: " + ex.getCause());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateTrustStore() {
        // Sends a message into the topic queue in order to refresh all the singleton instances of the CryptoService.
        jmsOperations.send(new ReloadTrustStoreMessageCreator());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void replaceTruststore(byte[] store, String password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        String truststoreFileValue = trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.file");
        File truststoreFile = new File(truststoreFileValue);
        if (!truststoreFile.getParentFile().exists()) {
            LOG.debug("Creating directory [" + truststoreFile.getParentFile() + "]");
            FileUtils.forceMkdir(truststoreFile.getParentFile());
        }
        LOG.debug("Replacing the existing truststore file [" + truststoreFileValue + "] with the provided one");

        loadTrustore(truststoreFileValue, password);
        FileOutputStream fileOutputStream = new FileOutputStream(truststoreFile);
        trustStore.store(fileOutputStream, trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.password").toCharArray());

       /* KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
        ts.load(new ByteArrayInputStream(store), password.toCharArray());
        FileOutputStream fileOutputStream = new FileOutputStream(truststoreFile);
        ts.store(fileOutputStream, trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.password").toCharArray());
        fileOutputStream.flush();
        fileOutputStream.close();
        trustStore = ts;*/
        updateTrustStore();

    }

    class ReloadTrustStoreMessageCreator implements MessageCreator {
        @Override
        public Message createMessage(Session session) throws JMSException {
            Message m = session.createMessage();
            m.setStringProperty(Command.COMMAND, Command.RELOAD_TRUSTSTORE);
            return m;
        }
    }

    public Certificate getCertificateFromKeystore(String alias) throws KeyStoreException {
        if (crypto != null && crypto.getKeyStore() != null) {
            return crypto.getKeyStore().getCertificate(alias);
        }

        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            String keyStoreFilename = keystoreProperties.getProperty("org.apache.ws.security.crypto.merlin.file");
            String keyStorePassword = keystoreProperties.getProperty("org.apache.ws.security.crypto.merlin.keystore.password");
            try (FileInputStream fileInputStream = new FileInputStream(keyStoreFilename)) {
                keyStore.load(fileInputStream, keyStorePassword.toCharArray());
                if (crypto != null) {
                    crypto.setKeyStore(keyStore);
                }
                return keyStore.getCertificate(alias);
            }
        } catch (Exception ex) {
            throw new KeyStoreException(ex);
        }
    }

    void setTrustStoreProperties(Properties trustStoreProperties) {
        this.trustStoreProperties = trustStoreProperties;
    }

    void setKeyStoreProperties(Properties keystoreProperties) {
        this.keystoreProperties = keystoreProperties;
    }

    public Merlin getCrypto() {
        return crypto;
    }
}
