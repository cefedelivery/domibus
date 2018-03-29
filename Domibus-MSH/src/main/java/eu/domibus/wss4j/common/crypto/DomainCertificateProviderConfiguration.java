package eu.domibus.wss4j.common.crypto;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Configuration
public class DomainCertificateProviderConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCertificateProviderConfiguration.class);

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DomainCertificateProviderImpl domainCertificateProviderImpl(Properties keystoreProperties, Properties trustStoreProperties) {
        final DomainCertificateProviderImpl domainCertificateProvider = new DomainCertificateProviderImpl();
        domainCertificateProvider.init(keystoreProperties, trustStoreProperties);
        return domainCertificateProvider;
    }


}
