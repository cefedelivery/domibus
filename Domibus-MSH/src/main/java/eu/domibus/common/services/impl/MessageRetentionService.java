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

package eu.domibus.common.services.impl;

import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.NotificationListener;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class MessageRetentionService {

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private BackendNotificationService backendNotificationService;

    // we could use any internal jmsOperations as we need to browse the queues anyways
    @Qualifier(value = "jmsTemplateNotify")
    @Autowired
    private JmsOperations jmsOperations;

    @Autowired
    AuthUtils authUtils;

    @Transactional
    public void deleteExpiredMessages() {
        final List<String> mpcs = pModeProvider.getMpcURIList();
        for (final String mpc : mpcs) {
            final int messageRetentionDownloaded = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
            if (messageRetentionDownloaded > 0) { // if -1 the messages will be kept indefinetely and if 0 it already has been deleted
                final List<String> messageIds = userMessageLogDao.getDownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionDownloaded * -1), mpc);
                delete(messageIds);
            }
            final int messageRetentionUndownloaded = pModeProvider.getRetentionUndownloadedByMpcURI(mpc);
            if (messageRetentionUndownloaded > -1) { // if -1 the messages will be kept indefinetely and if 0, although it makes no sense, is legal
                final List<String> messageIds = userMessageLogDao.getUndownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionUndownloaded * -1), mpc);
                delete(messageIds);
            }
        }
    }

    private void delete(List<String> messageIds) {
        for (final String messageId : messageIds) {
            if (backendNotificationService.getNotificationListenerServices() != null) {
                for (NotificationListener notificationListener : backendNotificationService.getNotificationListenerServices()) {
                    final String selector = MessageConstants.MESSAGE_ID + "= '" + messageId + "'";
                    boolean hasMessage = jmsOperations.browseSelected(notificationListener.getBackendNotificationQueue(), selector, new BrowserCallback<Boolean>() {
                        @Override
                        public Boolean doInJms(final Session session, final QueueBrowser browser) throws JMSException {
                            final Enumeration browserEnumeration = browser.getEnumeration();
                            if (browserEnumeration.hasMoreElements()) {
                                return true;
                            }
                            return false;
                        }
                    });
                    if (hasMessage) {
                        jmsOperations.receiveSelected(notificationListener.getBackendNotificationQueue(), selector);
                    }
                }
            }
            messagingDao.clearPayloadData(messageId);
            userMessageLogDao.setMessageAsDeleted(messageId);
            handleSignalMessageDelete(messageId);
        }
    }

    private void handleSignalMessageDelete(String messageId) {
        List<SignalMessage> signalMessages = signalMessageDao.findSignalMessagesByRefMessageId(messageId);
        if (!signalMessages.isEmpty()) {
            for (SignalMessage signalMessage : signalMessages) {
                signalMessageDao.clear(signalMessage);
            }
        }
        List<String> signalMessageIds = signalMessageDao.findSignalMessageIdsByRefMessageId(messageId);
        if (!signalMessageIds.isEmpty()) {
            for (String signalMessageId : signalMessageIds) {
                signalMessageLogDao.setMessageAsDeleted(signalMessageId);
            }
        }
    }

}
