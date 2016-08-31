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

package eu.domibus.messaging;

import eu.domibus.common.NotificationType;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class NotifyMessageCreator implements MessageCreator {

    private final String messageId;
    private NotificationType notificationType;
    private String finalRecipient;

    public NotifyMessageCreator(final String messageId, final NotificationType notificationType, final String finalRecipient) {
        this.messageId = messageId;
        this.notificationType = notificationType;
        this.finalRecipient = finalRecipient;
    }

    @Override
    public Message createMessage(final Session session) throws JMSException {
        final Message m = session.createMessage();
        m.setStringProperty(MessageConstants.MESSAGE_ID, messageId);
        m.setStringProperty(MessageConstants.NOTIFICATION_TYPE, notificationType.name());
        m.setStringProperty(MessageConstants.FINAL_RECIPIENT, finalRecipient);
        return m;
    }

    public String getMessageId() {
        return messageId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }
}
