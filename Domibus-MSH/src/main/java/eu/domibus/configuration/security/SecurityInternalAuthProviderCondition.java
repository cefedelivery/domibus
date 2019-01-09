package eu.domibus.configuration.security;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * {@code matches} method will return true if the authentication is provided internally
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Configuration
public class SecurityInternalAuthProviderCondition extends SecurityExternalAuthProviderCondition {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityInternalAuthProviderCondition.class);

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !super.matches(context, metadata);
    }
}
