package eu.domibus.configuration.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.configuration.pull.PullJmsListenerContainerFactoryFactory;
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
public class StorageFactoryImpl implements StorageFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(StorageFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public Storage create(Domain domain) {
        LOG.debug("Creating the StorageFactory for domain [{}]", domain);
        return (Storage)applicationContext.getBean("storage", domain);
    }

}
