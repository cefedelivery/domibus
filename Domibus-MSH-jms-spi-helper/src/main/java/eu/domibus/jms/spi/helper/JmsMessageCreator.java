package eu.domibus.jms.spi.helper;

import eu.domibus.jms.spi.InternalJmsMessage;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
public class JmsMessageCreator implements MessageCreator {

    private final InternalJmsMessage internalJmsMessage;

    public JmsMessageCreator(InternalJmsMessage message) {
        this.internalJmsMessage = message;
    }

    @Override
    public Message createMessage(Session session) throws JMSException {
        if(InternalJmsMessage.MessageType.MAP_MESSAGE == internalJmsMessage.getMessageType()) {
            return createMapMessage(session);
        }
        return createTextMessage(session);
    }

    private Message createMapMessage(Session session) throws JMSException {
        final MapMessage result = session.createMapMessage();
        if (internalJmsMessage.getType() != null) {
            result.setJMSType(internalJmsMessage.getType());
        }
        Map<String, Object> customProperties = internalJmsMessage.getCustomProperties();
        if (!customProperties.isEmpty()) {
            for (String pName : customProperties.keySet()) {
                Object pValue = customProperties.get(pName);
                result.setObjectProperty(pName, pValue);
            }
        }
        return result;
    }

    protected TextMessage createTextMessage(Session session) throws JMSException {
        TextMessage result = session.createTextMessage();

        result.setText(internalJmsMessage.getContent());
        if (internalJmsMessage.getType() != null) {
            result.setJMSType(internalJmsMessage.getType());
        }
        Map<String, Object> customProperties = internalJmsMessage.getCustomProperties();
        if (!customProperties.isEmpty()) {
            for (String pName : customProperties.keySet()) {
                Object pValue = customProperties.get(pName);
                result.setObjectProperty(pName, pValue);
            }
        }
        return result;
    }
}
