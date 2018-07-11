package eu.domibus.messaging;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class MessageListenerContainerInitializer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageListenerContainerInitializer.class);

    @Autowired
    protected MessageListenerContainerFactory messageListenerContainerFactory;

    @Autowired
    protected DomainService domainService;

    protected Map<Domain, MessageListenerContainer> instances = new HashMap<>();

    @PostConstruct
    public void init() {
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            createMessageListenerContainer(domain);
        }
    }

    /**
     * entry point method (post-construct)
     */
    public void createMessageListenerContainer(Domain domain) {
        MessageListenerContainer instance = messageListenerContainerFactory.createMessageListenerContainer(domain);
        instance.start();
        instances.put(domain, instance);
        LOG.info("MessageListenerContainer initialized for domain [{}]", domain);
    }


}
