package eu.domibus.common.validators;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.common.util.WarningUtil;
import eu.domibus.wss4j.common.crypto.api.MultiDomainCertificateProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Properties;

/**
 * Created by idragusa on 4/14/16.
 */
@Component
public class GatewayConfigurationValidator {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(GatewayConfigurationValidator.class);

    private static final String BLUE_GW_ALIAS = "blue_gw";

    @Resource(name = "trustStoreProperties")
    private Properties trustStoreProperties;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected MultiDomainCertificateProvider multiDomainCertificateProvider;

    @PostConstruct
    public void validateConfiguration() {
        LOG.info("Checking gateway configuration ...");
        validateCertificates();

        try {
            try (BufferedReader br = new BufferedReader((new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("domibus.properties.sha256"))));) {
                validateFileHash("domibus.properties", br.readLine());
            }
        } catch (Exception e) {
            LOG.warn("Could not verify the configuration files hash", e);
        }

    }

    protected void validateCertificates() {
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            validateCerts(domain);
        }

    }

    private void validateCerts(Domain domain) {
        multiDomainCertificateProvider.getTrustStore(domain);

        final KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            LOG.warn("Failed to get keystore instance! " + e.getMessage());
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.file"))) {
            ks.load(fileInputStream, trustStoreProperties.getProperty("org.apache.ws.security.crypto.merlin.trustStore.password").toCharArray());
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            LOG.warn("Failed to load certificates! " + e.getMessage());
            warnOutput("CERTIFICATES ARE NOT CONFIGURED PROPERLY - NOT FOR PRODUCTION USAGE");
        }

        try {
            if (ks.containsAlias(BLUE_GW_ALIAS)) {
                warnOutput("SAMPLE CERTIFICATES ARE BEING USED - NOT FOR PRODUCTION USAGE");
            }

        } catch (KeyStoreException e) {
            LOG.warn("Failed to load certificates! " + e.getMessage());
            warnOutput("CERTIFICATES ARE NOT CONFIGURED PROPERLY - NOT FOR PRODUCTION USAGE");
        }
    }

    private void validateFileHash(String filename, String expectedHash) throws IOException {
        File file = new File(domibusConfigurationService.getConfigLocation(), filename);
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
        LOG.warn(WarningUtil.warnOutput(message));
    }

}
