
package eu.domibus.plugin.jms;

import eu.domibus.common.NotificationType;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static eu.domibus.plugin.jms.JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;

/**
 * @author Christian Koch, Stefan Mueller
 */
class SignalMessageCreator implements MessageCreator {
    private NotificationType notificationType;
    private String messageId;

    public SignalMessageCreator(String messageId, NotificationType notificationType) {
        this.messageId = messageId;
        this.notificationType = notificationType;
    }

    @Override
    public Message createMessage(Session session) throws JMSException {
        Message message = session.createMapMessage();
        String messageType;
        if (this.notificationType == NotificationType.MESSAGE_SEND_SUCCESS) {
            messageType = JMSMessageConstants.MESSAGE_TYPE_SEND_SUCCESS;
        } else {
            throw new JMSException("unknown NotificationType: " + notificationType.name());
        }
        message.setStringProperty(JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, messageType);
        message.setStringProperty(MESSAGE_ID, messageId);
        return message;
    }
}
