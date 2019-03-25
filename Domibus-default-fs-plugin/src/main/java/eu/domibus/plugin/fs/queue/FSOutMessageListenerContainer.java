package eu.domibus.plugin.fs.queue;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

/**
 * FSPlugin Out {@code MessageListenerContainer} which implements {@code PluginMessageListenerContainer}
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Service
public class FSOutMessageListenerContainer implements PluginMessageListenerContainer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSOutMessageListenerContainer.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public MessageListenerContainer createDefaultMessageListenerContainer(DomainDTO domain) {
        LOG.debug("Creating the FSOutMessageListenerContainer for domain [{}]", domain);
        return (DefaultMessageListenerContainer) applicationContext.getBean("fsPluginOutContainer", domain);
    }
}
