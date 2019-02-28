package eu.domibus.weblogic.cluster;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@Configuration
public class ClusterDeploymentCondition implements ConfigurationCondition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ClusterDeploymentCondition.class);

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
        return Boolean.parseBoolean(environment.getProperty(DomibusConfigurationService.CLUSTER_DEPLOYMENT));
    }
}
