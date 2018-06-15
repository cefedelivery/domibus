
package eu.domibus.plugin.jms;

import eu.domibus.common.NotificationType;
import eu.domibus.ext.domain.JMSMessageDTOBuilder;
import eu.domibus.ext.domain.JmsMessageDTO;

import static eu.domibus.plugin.jms.JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public class SignalMessageCreator  {
    private NotificationType notificationType;
    private String messageId;

    public SignalMessageCreator(String messageId, NotificationType notificationType) {
        this.messageId = messageId;
        this.notificationType = notificationType;
    }


    public JmsMessageDTO createMessage() {
        final JMSMessageDTOBuilder jmsMessageBuilder = JMSMessageDTOBuilder.create();
        String messageType = null;
        if (this.notificationType == NotificationType.MESSAGE_SEND_SUCCESS) {
            messageType = JMSMessageConstants.MESSAGE_TYPE_SEND_SUCCESS;
        }
        jmsMessageBuilder.property(JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, messageType);
        jmsMessageBuilder.property(MESSAGE_ID, messageId);
        return jmsMessageBuilder.build();
    }
}
