package eu.domibus.configuration.pull;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ebms3.sender.PullMessageSender;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@DependsOn("springContextProvider")
@EnableJms
public class PullJmsListenerConfigurer implements JmsListenerConfigurer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullJmsListenerConfigurer.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected PullJmsListenerContainerFactoryFactory pullJmsListenerContainerFactoryFactory;

    protected Map<Domain, SimpleJmsListenerEndpoint> instances = new HashMap<>();

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        LOG.info("Initializing PullJmsListeners...");
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            LOG.info("Initializing PullJmsListener for domain [{}]", domain);
            createPullJmsListener(registrar, domain);
        }
    }

    private void createPullJmsListener(JmsListenerEndpointRegistrar registrar, Domain domain) {

        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();

        endpoint.setId("pullMessageSender_" + domain.getCode());
        endpoint.setDestination(domibusPropertyProvider.getProperty("domibus.jms.queue.pull"));

        endpoint.setMessageListener(new PullMessageSender()::processPullRequest);

        DefaultJmsListenerContainerFactory fact = pullJmsListenerContainerFactoryFactory.create(domain);

        AbstractMessageListenerContainer messageListenerContainer = fact.createListenerContainer(endpoint);
        messageListenerContainer.setMessageSelector(MessageConstants.DOMAIN + "='" + domain.getCode() + "'");

        endpoint.setupListenerContainer(messageListenerContainer);

        registrar.registerEndpoint(endpoint, fact);
        LOG.info("Jms Listener Endpoint registered for domain "+ domain.getCode());
    }
}