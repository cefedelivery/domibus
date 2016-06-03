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

package eu.domibus.ebms3.sender;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessageLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.model.logging.MessageLogEntry;
import eu.domibus.ebms3.common.DispatchMessageCreator;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class RetryService {

    private static final Log LOG = LogFactory.getLog(RetryService.class);

    public static final String TIMEOUT_TOLERANCE = "domibus.msh.retry.tolerance";
    public static final String UNRECOVERABLE_ERROR_RETRY = "domibus.dispatch.ebms.error.unrecoverable.retry";

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Autowired
    @Qualifier("jmsTemplateDispatch")
    private JmsOperations jmsOperations;

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue dispatchQueue;

    @Autowired
    private MessageLogDao messageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueueMessages() {
        final List<String> messageIdsToPurge = messageLogDao.findTimedoutMessages(Integer.parseInt(domibusProperties.getProperty(RetryService.TIMEOUT_TOLERANCE)));
        for (final String messageIdToPurge : messageIdsToPurge) {
            purgeTimedoutMessage(messageIdToPurge);
        }
        LOG.debug(messageIdsToPurge.size() + " messages to purge found");
        final List<String> messageIdsToSend = messageLogDao.findRetryMessages();
        if (!messageIdsToSend.isEmpty()) {
            jmsOperations.browse(dispatchQueue, new BrowserCallback<Void>() {
                @Override
                public Void doInJms(final Session session, final QueueBrowser browser) throws JMSException {
                    final Enumeration browserEnumeration = browser.getEnumeration();
                    while (browserEnumeration.hasMoreElements()) {
                        messageIdsToSend.remove(((Message) browserEnumeration.nextElement()).getStringProperty(MessageConstants.MESSAGE_ID));
                    }
                    return null;
                }
            });
            for (final String messageId : messageIdsToSend) {
                sendJmsMessage(messageId);
            }
        }
    }

    private void purgeTimedoutMessage(final String messageIdToPurge) {
        final MessageLogEntry messageLogEntry = messageLogDao.findByMessageId(messageIdToPurge, MSHRole.SENDING);

        final boolean notify = NotificationStatus.REQUIRED.equals(messageLogEntry.getNotificationStatus());

        if (notify) {
            backendNotificationService.notifyOfSendFailure(messageIdToPurge);
            messagingDao.delete(messageIdToPurge, MessageStatus.SEND_FAILURE, NotificationStatus.NOTIFIED);
        } else {
            messagingDao.delete(messageIdToPurge, MessageStatus.SEND_FAILURE);
        }
    }

    private void sendJmsMessage(final String messageId) {
        jmsOperations.send(dispatchQueue, new DispatchMessageCreator(messageId, messageLogDao.findEndpointForMessageId(messageId)));
    }
}
