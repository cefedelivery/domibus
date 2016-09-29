package eu.domibus.common.validators;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * Created by idragusa on 4/14/16.
 */
public class GatewayConfigurationValidator {

    private static final Log LOG = LogFactory.getLog(GatewayConfigurationValidator.class);

    private static final String BLUE_GW_ALIAS = "blue_gw";


    final String domibusConfigLocation = System.getProperty("domibus.config.location");

    @Resource(name = "trustStoreProperties")
    private Properties trustStoreProperties;

    @PostConstruct
    public void validateConfiguration() throws Exception {
        LOG.info("Checking gateway configuration ...");
        validateCerts();
        validateFileHash("domibus-datasources.xml", new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("domibus-datasources.xml.sha256"))).readLine());
        validateFileHash("domibus-security.xml", new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("domibus-security.xml.sha256"))).readLine());
    }

    private void validateCerts() {
        final KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            LOG.warn("Failed to get keystore instance! " + e.getMessage());
            LOG.debug(e);
            return;
        }

        try {
            ks.load(new FileInputStream(trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.file")), trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.password").toCharArray());
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            LOG.warn("Failed to load certificates! " + e.getMessage());
            LOG.debug(e);
            warnOutput("CERTIFICATES ARE NOT CONFIGURED PROPERLY - NOT FOR PRODUCTION USAGE");
        }
        try {
            if (ks.containsAlias(BLUE_GW_ALIAS)) {
                warnOutput("SAMPLE CERTIFICATES ARE BEING USED - NOT FOR PRODUCTION USAGE");
            }

        } catch (KeyStoreException e) {
            LOG.warn("Failed to load certificates! " + e.getMessage());
            LOG.debug(e);
            warnOutput("CERTIFICATES ARE NOT CONFIGURED PROPERLY - NOT FOR PRODUCTION USAGE");
        }
    }

    private void validateFileHash(String filename, String expectedHash) throws IOException {
        File file = new File(domibusConfigLocation + "/" + filename);
        try {
            String hash = DigestUtils.sha256Hex(FileUtils.readFileToByteArray(file));
            LOG.debug("Hash for " + filename + ": " + hash);
            if (hash.compareTo(expectedHash) == 0) {
                warnOutput("SAMPLE CONFIGURATION FILE IS BEING USED - NOT FOR PRODUCTION USAGE " + filename);
            }

        } catch (IOException e) {
            LOG.error("Failed to read configuration file " + filename + " " + e.getMessage());
            throw e;
        }
    }

    private void warnOutput(String message) {
        LOG.warn("\n\n\n");
        LOG.warn("**************** WARNING **************** WARNING **************** WARNING **************** ");
        LOG.warn(message);
        LOG.warn("*******************************************************************************************\n\n\n");
    }

}
