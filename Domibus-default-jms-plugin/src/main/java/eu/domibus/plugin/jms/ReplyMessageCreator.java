
package eu.domibus.plugin.jms;

import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

/**
 * @author Christian Koch, Stefan Mueller
 */
class ReplyMessageCreator implements MessageCreator {
    private String messageId;
    private String errorMessage;
    private String correlationId;

    ReplyMessageCreator(final String messageId, final String errorMessage, final String correlationId) {
        this.messageId = messageId;
        this.errorMessage = errorMessage;
        this.correlationId = correlationId;
    }

    @Override
    public Message createMessage(final Session session) throws JMSException {
        final MapMessage mapMessage = session.createMapMessage();
        mapMessage.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_SUBMIT_RESPONSE);
        mapMessage.setJMSCorrelationID(correlationId);
        if (messageId != null) {
            mapMessage.setStringProperty(JMSMessageConstants.MESSAGE_ID, messageId);
        }
        if (errorMessage != null) {
            mapMessage.setStringProperty("ErrorMessage", errorMessage);
        }
        return mapMessage;
    }
}