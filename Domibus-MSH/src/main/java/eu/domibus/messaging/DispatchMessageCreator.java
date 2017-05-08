package eu.domibus.messaging;

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;


/**
 * @author Christian Koch, Stefan Mueller
 */
public class DispatchMessageCreator {

    private final String messageId;

    public DispatchMessageCreator(final String messageId) {
        this.messageId = messageId;
    }

    public JmsMessage createMessage() {
        return JMSMessageBuilder
                .create()
                .property(MessageConstants.MESSAGE_ID, messageId)
                .build();
    }
}
