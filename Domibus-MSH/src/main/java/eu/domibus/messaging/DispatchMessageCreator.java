package eu.domibus.messaging;

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;


/**
 * @author Christian Koch, Stefan Mueller
 */
public class DispatchMessageCreator {

    private final String messageId;
    private final Domain domain;

    public DispatchMessageCreator(final String messageId, final Domain domain) {
        this.messageId = messageId;
        this.domain = domain;
    }

    public JmsMessage createMessage() {

        return JMSMessageBuilder
                .create()
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property(MessageConstants.DOMAIN, domain.getCode())
                .build();
    }
}
