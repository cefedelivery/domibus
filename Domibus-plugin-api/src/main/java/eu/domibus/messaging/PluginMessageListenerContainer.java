package eu.domibus.messaging;

import eu.domibus.ext.domain.DomainDTO;
import org.springframework.jms.listener.MessageListenerContainer;

public interface PluginMessageListenerContainer {

    MessageListenerContainer createDefaultMessageListenerContainer(DomainDTO domain);


}
