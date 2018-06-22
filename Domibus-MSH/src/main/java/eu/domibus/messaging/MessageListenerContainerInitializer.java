package eu.domibus.messaging;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.DomibusSchedulerFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @see
 * @since 4.0
 */
@Service
public class MessageListenerContainerInitializer {

    /**
     * logger
     */
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageListenerContainerInitializer.class);

    @Autowired
    protected MessageListenerContainerFactory messageListenerContainerFactory;

    @Autowired
    protected DomainService domainService;

    protected Map<Domain, MessageListenerContainer> instances = new HashMap<>();

    @PostConstruct
    public void init() {
        // Domain
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

        instances.put(domain, instance);
        LOG.info("Quartz scheduler started for domain [{}]", domain);
    }


}
