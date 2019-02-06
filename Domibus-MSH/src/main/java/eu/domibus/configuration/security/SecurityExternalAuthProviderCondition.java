package eu.domibus.configuration.security;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 *
 * {@code matches} method will return true if the authentication is provided by external provider (e.g. ECAS)
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Configuration
public class SecurityExternalAuthProviderCondition extends SecurityInternalAuthProviderCondition {

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !super.matches(context, metadata);
    }
}
