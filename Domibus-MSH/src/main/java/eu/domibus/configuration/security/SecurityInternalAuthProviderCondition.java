package eu.domibus.configuration.security;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * {@code matches} method will return true if the authentication is provided internally
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Configuration
@PropertySource(value = "file:///${domibus.config.location}/domibus.properties")
public class SecurityInternalAuthProviderCondition implements ConfigurationCondition {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityInternalAuthProviderCondition.class);

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final Environment environment = context.getEnvironment();
        if (environment == null) {
            LOG.debug("Condition not matching: environment is null");
            return false;
        }
        return !Boolean.parseBoolean(environment.getProperty(DomibusConfigurationService.EXTERNAL_AUTH_PROVIDER));
    }
}
