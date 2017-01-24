package eu.domibus.ebms3.common.model;

import eu.domibus.api.util.CollectionUtil;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.NotificationListener;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.Properties;

//TODO move this class out of the model package
/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class MessageRetentionService {

    private static final Log LOG = LogFactory.getLog(MessageRetentionService.class);

    public static final Integer DEFAULT_DOWNLOADED_MESSAGES_DELETE_LIMIT = 50;
    public static final Integer DEFAULT_NOT_DOWNLOADED_MESSAGES_DELETE_LIMIT = 50;
    public static final String DOWNLOADED_MESSAGES_DELETE_LIMIT_PROPERTY = "message.retention.downloaded.limit";
    public static final String NOT_DOWNLOADED_MESSAGES_DELETE_LIMIT_PROPERTY = "message.retention.not_downloaded.limit";

    @Autowired
    private CollectionUtil collectionUtil;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

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

    /**
     * Deletes the expired messages(downloaded or not) using the configured limits
     */
    @Transactional
    public void deleteExpiredMessages() {
        final List<String> mpcs = pModeProvider.getMpcURIList();
        final Integer expiredDownloadedMessagesLimit = getRetentionValue(DOWNLOADED_MESSAGES_DELETE_LIMIT_PROPERTY, DEFAULT_DOWNLOADED_MESSAGES_DELETE_LIMIT);
        final Integer expiredNotDownloadedMessagesLimit = getRetentionValue(NOT_DOWNLOADED_MESSAGES_DELETE_LIMIT_PROPERTY, DEFAULT_NOT_DOWNLOADED_MESSAGES_DELETE_LIMIT);
        for (final String mpc : mpcs) {
            deleteExpiredMessages(mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit);
        }
    }

    /**
     * Deletes all expired messages
     */
    @Transactional
    public void deleteAllExpiredMessages() {
        final List<String> mpcs = pModeProvider.getMpcURIList();
        final Integer expiredDownloadedMessagesLimit = Integer.MAX_VALUE;
        final Integer expiredNotDownloadedMessagesLimit = Integer.MAX_VALUE;;
        for (final String mpc : mpcs) {
            deleteExpiredMessages(mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit);
        }
    }

    @Transactional
    protected void deleteExpiredMessages(String mpc, Integer expiredDownloadedMessagesLimit, Integer expiredNotDownloadedMessagesLimit) {
        LOG.debug("Deleting expired messages for MPC [" + mpc + "] using expiredDownloadedMessagesLimit [" + expiredDownloadedMessagesLimit + "]" +
                " and expiredNotDownloadedMessagesLimit [" + expiredNotDownloadedMessagesLimit + "]");
        deleteExpiredDownloadedMessages(mpc, expiredDownloadedMessagesLimit);
        deleteExpiredNotDownloadedMessages(mpc, expiredNotDownloadedMessagesLimit);
    }

    protected void deleteExpiredDownloadedMessages(String mpc, Integer expiredDownloadedMessagesLimit) {
        LOG.debug("Deleting expired downloaded messages for MPC [" + mpc + "] using expiredDownloadedMessagesLimit [" + expiredDownloadedMessagesLimit + "]");
        final int messageRetentionDownloaded = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
        if (messageRetentionDownloaded > 0) { // if -1 the messages will be kept indefinetely and if 0 it already has been deleted
            List<String> downloadedMessageIds = userMessageLogDao.getDownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionDownloaded * -1), mpc);
            if (downloadedMessageIds != null && downloadedMessageIds.size() > 0) {
                LOG.debug("Found [" + downloadedMessageIds.size() + "] downloaded messages to delete");
                final Integer deleted = delete(downloadedMessageIds, expiredDownloadedMessagesLimit);
                LOG.debug("Deleted [" + deleted + "] downloaded messages");
            }
        }
    }

    protected void deleteExpiredNotDownloadedMessages(String mpc, Integer expiredNotDownloadedMessagesLimit) {
        LOG.debug("Deleting expired not-downloaded messages for MPC [" + mpc + "] using expiredNotDownloadedMessagesLimit [" + expiredNotDownloadedMessagesLimit + "]");
        final int messageRetentionNotDownloaded = pModeProvider.getRetentionUndownloadedByMpcURI(mpc);
        if (messageRetentionNotDownloaded > -1) { // if -1 the messages will be kept indefinetely and if 0, although it makes no sense, is legal
            final List<String> notDownloadedMessageIds = userMessageLogDao.getUndownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionNotDownloaded * -1), mpc);
            if (notDownloadedMessageIds != null && notDownloadedMessageIds.size() > 0) {
                LOG.debug("Found [" + notDownloadedMessageIds.size() + "] not-downloaded messages to delete");
                final Integer deleted = delete(notDownloadedMessageIds, expiredNotDownloadedMessagesLimit);
                LOG.debug("Deleted [" + deleted + "] not-downloaded messages");
            }
        }
    }

    protected Integer getRetentionValue(String propertyName, Integer defaultValue) {
        final String propertyValueString = domibusProperties.getProperty(propertyName);
        if (propertyValueString == null) {
            LOG.debug("Could not find property [" + propertyName + "]. Using the default value [" + defaultValue + "]");
            return defaultValue;
        }
        try {
            return Integer.parseInt(propertyValueString);
        } catch (NumberFormatException e) {
            LOG.warn("Could not parse value [" + propertyValueString + "] for property [" + propertyName + "]. Using default value [" + defaultValue + "]");
        }
        return defaultValue;
    }

    /* TODO it is not the responsibility of the MessageRetentionService to delete messages, the actual delete of the message should be delegated;
    move this method in the MessageService;*/
    protected Integer delete(List<String> messageIds, Integer limit) {
        List<String> toDelete = messageIds;
        if (messageIds.size() > limit) {
            LOG.debug("Only the first [" + limit + "] will be deleted");
            toDelete = collectionUtil.safeSubList(messageIds, 0, limit);
        }
        delete(toDelete);
        return toDelete.size();
    }

    /* TODO it is not the responsibility of the MessageRetentionService to delete messages, the actual delete of the message should be delegated;
    move this method in the MessageService;*/
    protected void delete(List<String> messageIds) {
        if (messageIds == null) {
            LOG.debug("Nothing to delete");
            return;
        }

        LOG.debug("Deleting [" + messageIds.size() + "] messages");
        for (final String messageId : messageIds) {
            deleteMessage(messageId);
        }
    }

    private void deleteMessage(String messageId) {
        LOG.debug("Deleting message [" + messageId + "]");
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
