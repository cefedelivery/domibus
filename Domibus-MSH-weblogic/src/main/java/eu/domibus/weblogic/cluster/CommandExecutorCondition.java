package eu.domibus.weblogic.cluster;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@Configuration
@PropertySource(value = "file:///${domibus.config.location}/domibus.properties")
public class CommandExecutorCondition implements ConfigurationCondition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(CommandExecutorCondition.class);

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
        return StringUtils.isNotBlank(environment.getProperty(DomibusConfigurationService.CLUSTER_DEPLOYMENT));
    }
}
