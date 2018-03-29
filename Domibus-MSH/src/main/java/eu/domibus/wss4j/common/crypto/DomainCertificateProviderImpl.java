package eu.domibus.wss4j.common.crypto;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.wss4j.common.crypto.api.CertificateProviderException;
import eu.domibus.wss4j.common.crypto.api.DomainCertificateProvider;
import eu.domibus.wss4j.common.crypto.api.DomainPropertyProvider;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */

public class DomainCertificateProviderImpl extends Merlin implements DomainCertificateProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCertificateProviderImpl.class);

    protected String domain;

    public DomainCertificateProviderImpl(){}

    public DomainCertificateProviderImpl(String domain){
        this.domain = domain;
    }

    @Autowired
    DomainPropertyProvider domainPropertyProvider;

    @PostConstruct
    public void init() {
        LOG.debug("Initializing the certificate provider");

        final Properties allProperties = new Properties();
        allProperties.putAll(getKeystoreProperties(domain));
        allProperties.putAll(getTruststoreProperties(domain));
        try {
            super.loadProperties(allProperties, Merlin.class.getClassLoader(), null);
        } catch (WSSecurityException | IOException e) {
            throw new CertificateProviderException(DomibusCoreErrorCode.DOM_001, "Error loading properties", e);
        }
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        return domainPropertyProvider.getPropertyValue(domain, "domibus.security.key.private.password");
    }

    protected Properties getKeystoreProperties(String domain) {
        final String keystoreType = domainPropertyProvider.getPropertyValue(domain, "domibus.security.keystore.type");
        final String keystorePassword = domainPropertyProvider.getPropertyValue(domain, "domibus.security.keystore.password");
        final String privateKeyAlias = domainPropertyProvider.getPropertyValue(domain, "domibus.security.key.private.alias");
        final String keystoreLocation = domainPropertyProvider.getResolvedPropertyValue(domain, "domibus.security.keystore.location");

        Properties result = new Properties();
        result.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", keystoreType);
        result.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", keystorePassword);
        result.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", privateKeyAlias);
        result.setProperty("org.apache.ws.security.crypto.merlin.file", keystoreLocation);

        LOG.debug("Keystore properties for domain [{}] are [{}]", domain, result);

        return result;
    }

    protected Properties getTruststoreProperties(String domain) {
        final String truststoreType = domainPropertyProvider.getPropertyValue(domain, "domibus.security.truststore.type");
        final String truststorePassword = domainPropertyProvider.getPropertyValue(domain, "domibus.security.truststore.password");
        final String truststoreLocation = domainPropertyProvider.getResolvedPropertyValue(domain, "domibus.security.truststore.location");

        Properties result = new Properties();
        result.setProperty("org.apache.ws.security.crypto.merlin.trustStore.type", truststoreType);
        result.setProperty("org.apache.ws.security.crypto.merlin.trustStore.password", truststorePassword);
        result.setProperty("org.apache.ws.security.crypto.merlin.load.cacerts", "false");
        result.setProperty("org.apache.ws.security.crypto.merlin.trustStore.file", truststoreLocation);

        LOG.debug("Truststore properties for domain [{}] are [{}]", domain, result);

        return result;
    }
}
