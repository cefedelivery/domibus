
package eu.domibus.plugin.jms;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.NotificationType;
import eu.domibus.ext.domain.JMSMessageDTOBuilder;
import eu.domibus.ext.domain.JmsMessageDTO;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;

/**
 * @author Christian Koch, Stefan Mueller
 */
class ErrorMessageCreator {

    private final ErrorResult errorResult;
    private final String endpoint;
    private final NotificationType notificationType;

    public ErrorMessageCreator(ErrorResult errorResult, String endpoint, NotificationType notificationType) {
        this.errorResult = errorResult;
        this.endpoint = endpoint;
        this.notificationType = notificationType;
    }

    public JmsMessageDTO createMessage() {
        final JMSMessageDTOBuilder jmsMessageBuilder = JMSMessageDTOBuilder.create();
        String messageType;
        switch (this.notificationType) {
            case MESSAGE_SEND_FAILURE:
                messageType = JMSMessageConstants.MESSAGE_TYPE_SEND_FAILURE;
                break;
            case MESSAGE_RECEIVED_FAILURE:
                messageType = JMSMessageConstants.MESSAGE_TYPE_RECEIVE_FAILURE;
                break;
            default:
                throw new DefaultJmsPluginException("unknown NotificationType: " + notificationType.name());
        }
        jmsMessageBuilder.property(JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, messageType);

        if (this.endpoint != null) {
            jmsMessageBuilder.property(PROPERTY_ENDPOINT, endpoint);
        }
        if (errorResult != null) {
            if (errorResult.getErrorCode() != null) {
                jmsMessageBuilder.property(JMSMessageConstants.ERROR_CODE, errorResult.getErrorCode().getErrorCodeName());
            }
            jmsMessageBuilder.property(JMSMessageConstants.ERROR_DETAIL, errorResult.getErrorDetail());
            jmsMessageBuilder.property(MESSAGE_ID, errorResult.getMessageInErrorId());
        }

        return jmsMessageBuilder.build();
    }
}