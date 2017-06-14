package eu.domibus;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class EmbeddedActiveMQBrokerCondition implements Condition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(EmbeddedActiveMQBrokerCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final Environment environment = context.getEnvironment();
        if (environment == null) {
            LOGGER.debug("Condition not matching: environment is null");
            return false;
        }
        final boolean embeddedActiveMQ = StringUtils.isNotEmpty(environment.getProperty("activeMQ.embedded.configurationFile"));
        return embeddedActiveMQ;
    }
}
