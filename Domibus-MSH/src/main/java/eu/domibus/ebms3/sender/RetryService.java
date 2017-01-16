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

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.ebms3.common.model.DispatchMessageCreator;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
    public static final String TIMEOUT_TOLERANCE = "domibus.msh.retry.tolerance";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetryService.class);
    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Autowired
    @Qualifier("jmsTemplateDispatch")
    private JmsOperations jmsOperations;

    @Autowired
    JMSManager jmsManager;

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue dispatchQueue;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueueMessages() {
        final List<String> messageIdsToPurge = userMessageLogDao.findTimedoutMessages(Integer.parseInt(domibusProperties.getProperty(RetryService.TIMEOUT_TOLERANCE)));
        for (final String messageIdToPurge : messageIdsToPurge) {
            purgeTimedoutMessage(messageIdToPurge);
        }
        LOG.debug(messageIdsToPurge.size() + " messages to purge found");
        final List<String> messageIdsToSend = userMessageLogDao.findRetryMessages();
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
        final MessageLog userMessageLog = userMessageLogDao.findByMessageId(messageIdToPurge, MSHRole.SENDING);

        final boolean notify = NotificationStatus.REQUIRED.equals(userMessageLog.getNotificationStatus());

        if (notify) {
            backendNotificationService.notifyOfSendFailure(messageIdToPurge);
            messagingDao.delete(messageIdToPurge, MessageStatus.SEND_FAILURE, NotificationStatus.NOTIFIED);
        } else {
            messagingDao.clearPayloadData(messageIdToPurge);
            userMessageLogDao.setMessageAsSendFailure(messageIdToPurge);
        }
    }

    private void sendJmsMessage(final String messageId) {
        jmsManager.sendMessageToQueue(new DispatchMessageCreator(messageId, userMessageLogDao.findEndpointForMessageId(messageId)).createMessage(), dispatchQueue);
    }
}
