package eu.domibus.messaging;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class MessageListenerContainerFactoryImpl implements MessageListenerContainerFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageListenerContainerFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public MessageListenerContainer createMessageListenerContainer(Domain domain) {
        LOG.debug("Creating the MessageListenerContainer for domain [{}]", domain);

        return applicationContext.getBean(DefaultMessageListenerContainer.class, domain);
    }

}
