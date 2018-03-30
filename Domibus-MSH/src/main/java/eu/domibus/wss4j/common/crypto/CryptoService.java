package eu.domibus.wss4j.common.crypto;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
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

    private KeyStore keyStore;

    private Object kestoreLock = new Object();

    public synchronized KeyStore getTrustStore() {
        if (trustStore == null) {
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                loadTrustStore();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | WSSecurityException e) {
                LOG.error("Error while initializing trustStore", e);
            }
        }
        return trustStore;
    }

    public void setJmsOperations(JmsOperations jmsOperations) {
        this.jmsOperations = jmsOperations;
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
        boolean containsAlias;
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

    private void loadTrustStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, WSSecurityException {
        if (trustStore == null) {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        }
        LOG.info("Initiating truststore");
        String trustStoreFilename = trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.file");
        String trustStorePassword = trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.password");
        try (final FileInputStream strustStoreStream = new FileInputStream(trustStoreFilename)) {
            trustStore.load(strustStoreStream, trustStorePassword.toCharArray());
        }
    }

    public Certificate getCertificateFromKeystore(String alias) throws KeyStoreException {
        initKeystore();
        return keyStore.getCertificate(alias);
    }

    public KeyStore getKeyStore() {
        try {
            initKeystore();
        } catch (KeyStoreException e) {
            throw new eu.domibus.api.security.CertificateException(DomibusCoreErrorCode.DOM_005,e.getMessage());
        }
        return keyStore;
    }

    public void initKeystore() throws KeyStoreException {
        if (keyStore == null) {
            try {
                synchronized (kestoreLock) {
                    if (keyStore != null) return;
                    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

                    String keyStoreFilename = keystoreProperties.getProperty("org.apache.ws.security.crypto.merlin.file");
                    String keyStorePassword = keystoreProperties.getProperty("org.apache.ws.security.crypto.merlin.keystore.password");
                    try (FileInputStream fileInputStream = new FileInputStream(keyStoreFilename)) {
                        keyStore.load(fileInputStream, keyStorePassword.toCharArray());
                    }
                }
            } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException ex) {
                throw new KeyStoreException(ex);
            }
        }
    }

    public void setTrustStoreProperties(Properties trustStoreProperties) {
        this.trustStoreProperties = trustStoreProperties;
    }

    void setKeyStoreProperties(Properties keystoreProperties) {
        this.keystoreProperties = keystoreProperties;
    }

}
