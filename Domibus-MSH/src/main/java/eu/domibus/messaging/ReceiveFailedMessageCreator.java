package eu.domibus.messaging;

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.NotificationType;

/**
 * Created by kochc01 on 11.03.2016.
 */
public class ReceiveFailedMessageCreator {

    private final String messageId;
    private final String endpoint;


    public ReceiveFailedMessageCreator(final String messageId, final String endpoint) {
        this.messageId = messageId;
        this.endpoint = endpoint;
    }

    public JmsMessage createMessage()  {
        return JMSMessageBuilder
                .create()
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED_FAILURE.name())
                .property(MessageConstants.ENDPOINT, endpoint)
                .build();
    }
}
