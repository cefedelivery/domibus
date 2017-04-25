package eu.domibus.messaging;

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;


/**
 * @author Christian Koch, Stefan Mueller
 */
public class DispatchMessageCreator {

    private final String messageId;
    private final String endpoint;

    public DispatchMessageCreator(final String messageId, final String endpoint) {
        this.messageId = messageId;
        this.endpoint = endpoint;
    }

    public JmsMessage createMessage() {
        return JMSMessageBuilder
                .create()
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property(MessageConstants.ENDPOINT, endpoint)
                .build();
    }
}
