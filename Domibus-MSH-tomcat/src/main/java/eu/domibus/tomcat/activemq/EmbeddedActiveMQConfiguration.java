package eu.domibus.tomcat.activemq;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Configuration
@PropertySource(value="file:///${domibus.config.location}/domibus.properties")
public class EmbeddedActiveMQConfiguration implements Condition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(EmbeddedActiveMQConfiguration.class);

    @Value("${activeMQ.embedded.configurationFile}")
    Resource activeMQConfiguration;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final boolean embeddedActiveMQBroker = new EmbeddedActiveMQBrokerCondition().matches(context, metadata);
        LOGGER.debug("Condition result is [{}]", embeddedActiveMQBroker);
        return embeddedActiveMQBroker;
    }

    @Bean(name = "broker")
    @Conditional(EmbeddedActiveMQConfiguration.class)
    public BrokerFactoryBean activeMQBroker() {
        LOGGER.debug("Creating the embedded Active MQ broker from [{}]", activeMQConfiguration);
        final BrokerFactoryBean brokerFactoryBean = new BrokerFactoryBean();
        brokerFactoryBean.setConfig(activeMQConfiguration);
        return brokerFactoryBean;

    }
}
