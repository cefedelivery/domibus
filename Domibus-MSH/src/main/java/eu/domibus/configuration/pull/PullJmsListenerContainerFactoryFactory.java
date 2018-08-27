package eu.domibus.configuration.pull;

import eu.domibus.api.multitenancy.Domain;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public interface PullJmsListenerContainerFactoryFactory {

    DefaultJmsListenerContainerFactory create(Domain domain);

}
