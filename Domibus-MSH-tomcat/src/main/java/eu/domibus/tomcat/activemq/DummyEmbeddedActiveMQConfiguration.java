package eu.domibus.tomcat.activemq;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Configuration
@PropertySource(value = "file:///${domibus.config.location}/domibus.properties")
public class DummyEmbeddedActiveMQConfiguration implements Condition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DummyEmbeddedActiveMQConfiguration.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final boolean dummyActiveMQBroker = !new EmbeddedActiveMQBrokerCondition().matches(context, metadata);
        LOGGER.debug("Condition result is [{}]", dummyActiveMQBroker);
        return dummyActiveMQBroker;
    }

    @Bean(name = "broker")
    @Conditional(DummyEmbeddedActiveMQConfiguration.class)
    public Object activeMQBroker() {
        LOGGER.debug("Creating a dummy bean to satisfy the depends-on dependencies");
        return new Object();
    }
}
