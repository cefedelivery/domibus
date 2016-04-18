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

package eu.domibus.common.dao;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Date;

/**
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

@Repository
public class MessagingDao extends BasicDao<Messaging> {

    public MessagingDao() {
        super(Messaging.class);
    }

    public UserMessage findUserMessageByMessageId(final String messageId) {

        final TypedQuery<UserMessage> query = this.em.createNamedQuery("Messaging.findUserMessageByMessageId", UserMessage.class);
        query.setParameter("MESSAGE_ID", messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }

    public SignalMessage findSignalMessageByMessageId(final String messageId) {

        final TypedQuery<SignalMessage> query = this.em.createNamedQuery("Messaging.findSignalMessageByMessageId", SignalMessage.class);
        query.setParameter("MESSAGE_ID", messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }

    /**
     * Removes the binary payload data of a given
     * {@link eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage} and sets
     * status to {@link eu.domibus.common.MessageStatus#DELETED}.
     *
     * @param messaging the message to be updated
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(final Messaging messaging) {
        final String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
        delete(messageId);
    }

    /**
     * Clear payloaddata for corresponding message with the given messageId. Set MessageStatus to DELETED
     *
     * @param messageId
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(final String messageId) {
        delete(messageId, MessageStatus.DELETED);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(final String messageId, final MessageStatus messageStatus) {
        clearPayloadData(messageId);

        final Query messageStatusQuery = this.em.createNamedQuery("MessageLogEntry.setMessageStatus");
        messageStatusQuery.setParameter("MESSAGE_ID", messageId);
        messageStatusQuery.setParameter("TIMESTAMP", new Date());
        messageStatusQuery.setParameter("MESSAGE_STATUS", messageStatus);
        messageStatusQuery.executeUpdate();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(final String messageId, final MessageStatus messageStatus, final NotificationStatus notificationStatus) {
        clearPayloadData(messageId);

        final Query messageStatusQuery = this.em.createNamedQuery("MessageLogEntry.setMessageStatusAndNotificationStatus");
        messageStatusQuery.setParameter("MESSAGE_ID", messageId);
        messageStatusQuery.setParameter("TIMESTAMP", new Date());
        messageStatusQuery.setParameter("MESSAGE_STATUS", messageStatus);
        messageStatusQuery.setParameter("NOTIFICATION_STATUS", notificationStatus);
        messageStatusQuery.executeUpdate();
    }

    private void clearPayloadData(final String messageId) {
        final Query payloadsQuery = em.createNamedQuery("Messaging.findPartInfosForMessage");
        payloadsQuery.setParameter("MESSAGE_ID", messageId);

        if(payloadsQuery.getResultList().isEmpty())
            return;

        final Query emptyQuery = em.createNamedQuery("Messaging.emptyPayloads");
        emptyQuery.setParameter("PARTINFOS", payloadsQuery.getResultList());
        emptyQuery.executeUpdate();
    }

}
