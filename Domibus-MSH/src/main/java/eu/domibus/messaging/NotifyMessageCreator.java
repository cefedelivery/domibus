package eu.domibus.messaging;

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.NotificationType;

import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class NotifyMessageCreator {

    private final String messageId;
    private NotificationType notificationType;
    private Map<String, Object> properties;

    public NotifyMessageCreator(final String messageId, final NotificationType notificationType, final Map<String, Object> properties) {
        this.messageId = messageId;
        this.notificationType = notificationType;
        this.properties = properties;
    }

    public JmsMessage createMessage() {
        final JMSMessageBuilder jmsMessageBuilder = JMSMessageBuilder.create();
        if (properties != null) {
            jmsMessageBuilder.properties(properties);
        }
        jmsMessageBuilder.property(MessageConstants.MESSAGE_ID, messageId);
        jmsMessageBuilder.property(MessageConstants.NOTIFICATION_TYPE, notificationType.name());

        return jmsMessageBuilder.build();
    }

    public String getMessageId() {
        return messageId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }
}
