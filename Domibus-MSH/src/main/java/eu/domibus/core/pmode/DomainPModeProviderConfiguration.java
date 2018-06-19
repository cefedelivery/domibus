package eu.domibus.core.pmode;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class DomainPModeProviderConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainPModeProviderConfiguration.class);

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DynamicDiscoveryPModeProvider domainPModeProvider(Domain domain) {
        LOG.debug("Instantiating the PMode provider for domain [{}]", domain);

        final DynamicDiscoveryPModeProvider pModeProvider = new DynamicDiscoveryPModeProvider(domain);
        return pModeProvider;
    }


}
