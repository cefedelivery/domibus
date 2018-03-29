package eu.domibus.wss4j.common.crypto;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.wss4j.common.crypto.api.CertificateProviderException;
import eu.domibus.wss4j.common.crypto.api.DomainCertificateProvider;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */

public class DomainCertificateProviderImpl extends Merlin implements DomainCertificateProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCertificateProviderImpl.class);


    public void init(Properties keystoreProperties, Properties truststoreProperties) {
        LOG.debug("Initializing the certificate provider");

        final Properties allProperties = new Properties();
        allProperties.putAll(keystoreProperties);
        allProperties.putAll(truststoreProperties);
        try {
            super.loadProperties(allProperties, Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CertificateProviderException(DomibusCoreErrorCode.DOM_001, "Error loading properties", e);
        }

    }
}
