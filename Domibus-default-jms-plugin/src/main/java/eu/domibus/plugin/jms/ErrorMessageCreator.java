/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.plugin.jms;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.NotificationType;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;

/**
 * @author Christian Koch, Stefan Mueller
 */
class ErrorMessageCreator implements MessageCreator {

    private final ErrorResult errorResult;
    private final String endpoint;
    private final NotificationType notificationType;

    public ErrorMessageCreator(ErrorResult errorResult, String endpoint, NotificationType notificationType) {
        this.errorResult = errorResult;
        this.endpoint = endpoint;
        this.notificationType = notificationType;
    }

    @Override
    public Message createMessage(Session session) throws JMSException {
        Message message = session.createMapMessage();
        String messageType;
        switch (this.notificationType) {
            case MESSAGE_SEND_FAILURE:
                messageType = JMSMessageConstants.MESSAGE_TYPE_SEND_FAILURE;
                break;
            case MESSAGE_RECEIVED_FAILURE:
                messageType = JMSMessageConstants.MESSAGE_TYPE_RECEIVE_FAILURE;
                break;
            default:
                throw new JMSException("unknown NotificationType: " + notificationType.name());
        }
        message.setStringProperty(JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, messageType);

        if (this.endpoint != null) {
            message.setStringProperty(PROPERTY_ENDPOINT, endpoint);
        }
        if(errorResult != null) {
            if(errorResult.getErrorCode() != null) {
                message.setStringProperty(JMSMessageConstants.ERROR_CODE, errorResult.getErrorCode().getErrorCodeName());
            }
            message.setStringProperty(JMSMessageConstants.ERROR_DETAIL, errorResult.getErrorDetail());
            message.setStringProperty(MESSAGE_ID, errorResult.getMessageInErrorId());
        }

        return message;
    }
}