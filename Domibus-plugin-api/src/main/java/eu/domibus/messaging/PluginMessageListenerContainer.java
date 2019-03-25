package eu.domibus.messaging;

import eu.domibus.ext.domain.DomainDTO;
import org.springframework.jms.listener.MessageListenerContainer;

/**
 * The purpose of the interface is to define a contract for creating {@code MessageListenerContainer}
 * at plugins level
 * The initialization will be done in {@code MessageListenerContainerInitializer} from core
 *
 * @author Catalin Enache
 * @since 4.1
 */
public interface PluginMessageListenerContainer {

    MessageListenerContainer createDefaultMessageListenerContainer(DomainDTO domain);
}
