package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class MultiTenantAwareEntityManagerCondition implements ConfigurationCondition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(MultiTenantAwareEntityManagerCondition.class);

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final Environment environment = context.getEnvironment();
        if (environment == null) {
            LOGGER.debug("Condition not matching: environment is null");
            return false;
        }
        final boolean isMultiTenantAware = StringUtils.isNotEmpty(environment.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY));
        return isMultiTenantAware;
    }
}
