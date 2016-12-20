package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.jms.spi.InternalJmsMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class JMSMessageMapper {

    public InternalJmsMessage convert(JmsMessage message) {
        InternalJmsMessage result = new InternalJmsMessage();
        result.setId(message.getId());
        result.setContent(message.getContent());
        result.setTimestamp(message.getTimestamp());
        result.setType(message.getType());
        result.setProperties(message.getProperties());
        return result;
    }

    public JmsMessage convert(InternalJmsMessage message) {
        JmsMessage result = new JmsMessage();
        result.setId(message.getId());
        result.setContent(message.getContent());
        result.setTimestamp(message.getTimestamp());
        result.setType(message.getType());
        result.setProperties(message.getProperties());
        return result;
    }

    public List<JmsMessage> convert(List<InternalJmsMessage> messagesSPI) {
        List<JmsMessage> result = new ArrayList<>();
        for (InternalJmsMessage internalJmsMessage : messagesSPI) {
            result.add(convert(internalJmsMessage));
        }
        return result;
    }
}
