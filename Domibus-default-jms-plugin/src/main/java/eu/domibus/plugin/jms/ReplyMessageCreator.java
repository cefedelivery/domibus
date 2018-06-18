
package eu.domibus.plugin.jms;

import eu.domibus.ext.domain.JMSMessageDTOBuilder;
import eu.domibus.ext.domain.JmsMessageDTO;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class ReplyMessageCreator {
    private String messageId;
    private String errorMessage;
    private String correlationId;

    ReplyMessageCreator(final String messageId, final String errorMessage, final String correlationId) {
        this.messageId = messageId;
        this.errorMessage = errorMessage;
        this.correlationId = correlationId;
    }


    public JmsMessageDTO createMessage() {
        final JMSMessageDTOBuilder jmsMessageBuilder = JMSMessageDTOBuilder.create();
        jmsMessageBuilder.property(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_SUBMIT_RESPONSE);
        jmsMessageBuilder.jmsCorrelationId(correlationId);
        if (messageId != null) {
            jmsMessageBuilder.property(JMSMessageConstants.MESSAGE_ID, messageId);
        }
        if (errorMessage != null) {
            jmsMessageBuilder.property("ErrorMessage", errorMessage);
        }
        return jmsMessageBuilder.build();
    }
}