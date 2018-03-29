package eu.domibus.wss4j.common.crypto;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.property.PropertyResolver;
import eu.domibus.spring.SpringContextProvider;
import eu.domibus.wss4j.common.crypto.api.DomainCertificateProvider;
import eu.domibus.wss4j.common.crypto.api.DomainCertificateProviderFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class DomainCertificateProviderFactoryImpl implements DomainCertificateProviderFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCertificateProviderFactoryImpl.class);

    @Autowired
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Override
    public DomainCertificateProvider createDomainCertificateProvider(String domain) {
        LOG.debug("Creating the certificate provider for domain [{}]", domain);

        final Properties keystoreProperties = getKeystoreProperties(domain);
        final Properties truststoreProperties = getTruststoreProperties(domain);
        return SpringContextProvider.getApplicationContext().getBean(DomainCertificateProviderImpl.class, keystoreProperties, truststoreProperties);
    }

    protected String getDomainCertificateProviderBeanName(String domain) {
        String prefix = "";
        if (StringUtils.isNotEmpty(domain)) {
            prefix = domain;
        }
        return prefix + "DomainCertificateProviderBean";
    }

    protected Properties getKeystoreProperties(String domain) {
        String prefix = "";
        if (StringUtils.isNotEmpty(domain)) {
            prefix = domain + ".";
        }

        final String keystoreType = domibusProperties.getProperty(prefix + "domibus.security.keystore.type");
        final String keystorePassword = domibusProperties.getProperty(prefix + "domibus.security.keystore.password");
        final String keystoreAlias = domibusProperties.getProperty(prefix + "domibus.security.key.private.alias");

        PropertyResolver propertyResolver = new PropertyResolver();
        String keystoreLocation = domibusProperties.getProperty(prefix + "domibus.security.keystore.location");
        keystoreLocation = propertyResolver.getResolvedValue(keystoreLocation);

        Properties result = new Properties();
        result.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", keystoreType);
        result.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", keystorePassword);
        result.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", keystoreAlias);
        result.setProperty("org.apache.ws.security.crypto.merlin.file", keystoreLocation);

        LOG.debug("Keystore properties for domain [{}] are [{}]", domain, result);

        return result;
    }

    protected Properties getTruststoreProperties(String domain) {
        String prefix = "";
        if (StringUtils.isNotEmpty(domain)) {
            prefix = domain + ".";
        }

        final String truststoreType = domibusProperties.getProperty(prefix + "domibus.security.truststore.type");
        final String truststorePassword = domibusProperties.getProperty(prefix + "domibus.security.truststore.password");
        final String truststoreLocation = domibusProperties.getProperty(prefix + "domibus.security.truststore.location");

        Properties result = new Properties();
        result.setProperty("org.apache.ws.security.crypto.merlin.trustStore.type", truststoreType);
        result.setProperty("org.apache.ws.security.crypto.merlin.trustStore.password", truststorePassword);
        result.setProperty("org.apache.ws.security.crypto.merlin.load.cacerts", "false");
        result.setProperty("org.apache.ws.security.crypto.merlin.trustStore.file", truststoreLocation);

        LOG.debug("Truststore properties for domain [{}] are [{}]", domain, result);

        return result;
    }


}
