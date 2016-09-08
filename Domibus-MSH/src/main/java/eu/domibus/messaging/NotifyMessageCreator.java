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

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.NotificationType;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class NotifyMessageCreator  {

    private final String messageId;
    private NotificationType notificationType;

    public NotifyMessageCreator(final String messageId, final NotificationType notificationType) {
        this.messageId = messageId;
        this.notificationType = notificationType;
    }

    public JmsMessage createMessage()  {
        return JMSMessageBuilder
                .create()
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property(MessageConstants.NOTIFICATION_TYPE, notificationType.name())
                .build();
    }

    public String getMessageId() {
        return messageId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }
}
