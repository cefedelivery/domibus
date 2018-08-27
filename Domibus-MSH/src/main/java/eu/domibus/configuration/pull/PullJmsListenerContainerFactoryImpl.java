package eu.domibus.configuration.pull;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class PullJmsListenerContainerFactoryImpl implements PullJmsListenerContainerFactoryFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullJmsListenerContainerFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public DefaultJmsListenerContainerFactory create(Domain domain) {
        LOG.debug("Creating the PullJmsListenerContainerFactory for domain [{}]", domain);
        return (DefaultJmsListenerContainerFactory)applicationContext.getBean("pullJmsListenerContainerFactory", domain);
    }

}
