package eu.domibus.configuration.storage;

import eu.domibus.api.multitenancy.Domain;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public interface StorageFactory {

    Storage create(Domain domain);

}
