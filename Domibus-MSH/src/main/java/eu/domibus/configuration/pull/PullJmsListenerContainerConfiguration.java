package eu.domibus.configuration.pull;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import java.util.Optional;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Configuration
public class PullJmsListenerContainerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullJmsListenerContainerConfiguration.class);
    private static final String PULL_QUEUE_CONCURRENCY = "domibus.pull.queue.concurency";

    @Autowired
    @Qualifier("domibusJMS-XAConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    Optional<JndiDestinationResolver> internalDestinationResolver;

    @Bean(name = "pullJmsListenerContainerFactory")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultJmsListenerContainerFactory pullJmsListenerContainerFactory(Domain domain) {
        return create(domain);
    }

    protected DefaultJmsListenerContainerFactory create(Domain domain) {
        LOG.trace("creating pullJmsListenerContainerFactory for domain [{}] and with internalDestinationREsolver [{}]",
                domain, internalDestinationResolver);

        DefaultJmsListenerContainerFactory messageListenerContainerFactory = new DefaultJmsListenerContainerFactory();

        messageListenerContainerFactory.setConnectionFactory(connectionFactory);
        messageListenerContainerFactory.setTransactionManager(transactionManager);

        String concurrency = domibusPropertyProvider.getDomainProperty(domain, PULL_QUEUE_CONCURRENCY);
        messageListenerContainerFactory.setConcurrency(concurrency);

        messageListenerContainerFactory.setSessionTransacted(true);
        messageListenerContainerFactory.setSessionAcknowledgeMode(0);

        if(internalDestinationResolver.isPresent()) {
            messageListenerContainerFactory.setDestinationResolver(internalDestinationResolver.get());
        }

        return messageListenerContainerFactory;
    }

}
