package eu.domibus.common.validators;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    private static final String MD5_DOMIBUS_DATASOURCE_XML = "3aecbfa79b63d039a2fc05b26311c5ac";
    private static final String MD5_DOMIBUS_SECURITY_XML = "6c999533a80fdcbb3755fb1c715644de";


    final String domibusConfigLocation = System.getProperty("domibus.config.location");


    @Resource(name = "truststoreProperties")
    private Properties truststoreProperties;

    @PostConstruct
    public void validateConfiguration() throws Exception {
        LOG.info("Checking gateway configuration ...");
        validateCerts();
        validateFileHash("domibus-datasources.xml", MD5_DOMIBUS_DATASOURCE_XML);
        validateFileHash("domibus-security.xml", MD5_DOMIBUS_SECURITY_XML);
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
            ks.load(new FileInputStream(truststoreProperties.getProperty("org.apache.ws.security.crypto.merlin.truststore.file")), truststoreProperties.getProperty("org.apache.ws.security.crypto.merlin.truststore.password").toCharArray());
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            LOG.warn("Failed to load certificates! " + e.getMessage());
            LOG.debug(e);
            warnOutput("CERTIFICATES ARE NOT CONFIGURED PROPERLY - NOT FOR PRODUCTION USAGE");
        }
        try {
            if(ks.containsAlias(BLUE_GW_ALIAS)) {
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
            String hash  = DigestUtils.md5Hex(FileUtils.readFileToByteArray(file));
            LOG.debug("Hash for " + filename + ": " + hash);
            if(hash.compareTo(expectedHash) == 0) {
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
