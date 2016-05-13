package eu.domibus.messaging;

import eu.domibus.common.NotificationType;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Created by kochc01 on 11.03.2016.
 */
public class ReceiveFailedMessageCreator implements MessageCreator {

    private final String messageId;
    private final String endpoint;


    public ReceiveFailedMessageCreator(final String messageId, final String endpoint) {
        this.messageId = messageId;

        this.endpoint = endpoint;
    }

    @Override
    public Message createMessage(final Session session) throws JMSException {
        final Message m = session.createMessage();
        m.setStringProperty(MessageConstants.MESSAGE_ID, messageId);
        m.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED_FAILURE.name());
        m.setStringProperty(MessageConstants.ENDPOINT, endpoint);
        return m;
    }
}
