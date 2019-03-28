package eu.domibus.common.services.impl;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.util.Date;
import java.util.List;

/**
 * This service class is responsible for the retention and clean up of Domibus messages, including signal messages.
 * Notice that only payloads data are really deleted.
 *
 * @author Christian Koch, Stefan Mueller, Federico Martini, Cosmin Baciu
 * @since 3.0
 */
@Service
public class MessageRetentionDefaultService implements MessageRetentionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageRetentionDefaultService.class);

    public static final String DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE = "domibus.retentionWorker.message.retention.downloaded.max.delete";
    public static final String DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE = "domibus.retentionWorker.message.retention.not_downloaded.max.delete";
    public static final String DOMIBUS_ATTACHMENT_STORAGE_LOCATION = "domibus.attachment.storage.location";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private JMSManager jmsManager;

    @Autowired
    @Qualifier("retentionMessageQueue")
    private Queue retentionMessageQueue;


    /**
     * Deletes the expired messages(downloaded or not) using the configured limits
     */
    @Override
    @Transactional
    public void deleteExpiredMessages() {
        final List<String> mpcs = pModeProvider.getMpcURIList();
        final Integer expiredDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
        final Integer expiredNotDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
        for (final String mpc : mpcs) {
            deleteExpiredMessages(mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit);
        }
    }

    @Override
    @Transactional
    public void deleteExpiredMessages(String mpc, Integer expiredDownloadedMessagesLimit, Integer expiredNotDownloadedMessagesLimit) {
        LOG.debug("Deleting expired messages for MPC [{}] using expiredDownloadedMessagesLimit [{}]" +
                " and expiredNotDownloadedMessagesLimit [{}]", mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit);
        deleteExpiredDownloadedMessages(mpc, expiredDownloadedMessagesLimit);
        deleteExpiredNotDownloadedMessages(mpc, expiredNotDownloadedMessagesLimit);
    }

    protected void deleteExpiredDownloadedMessages(String mpc, Integer expiredDownloadedMessagesLimit) {
        LOG.debug("Deleting expired downloaded messages for MPC [{}] using expiredDownloadedMessagesLimit [{}]", mpc, expiredDownloadedMessagesLimit);
        final int messageRetentionDownloaded = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
        String fileLocation = domibusPropertyProvider.getDomainProperty(DOMIBUS_ATTACHMENT_STORAGE_LOCATION);
        // If messageRetentionDownloaded is equal to -1, the messages will be kept indefinitely and, if 0 and no file system storage was used, they have already been deleted during download operation.
        if (messageRetentionDownloaded > 0 || (StringUtils.isNotEmpty(fileLocation) && messageRetentionDownloaded >= 0)) {
            List<String> downloadedMessageIds = userMessageLogDao.getDownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionDownloaded * -1),
                    mpc, expiredDownloadedMessagesLimit);
            if (CollectionUtils.isNotEmpty(downloadedMessageIds)) {
                final int deleted = downloadedMessageIds.size();
                LOG.debug("Found [{}] downloaded messages to delete", deleted);
                scheduleDeleteMessages(downloadedMessageIds);
                LOG.debug("Deleted [{}] downloaded messages", deleted);
            }
        }
    }

    protected void deleteExpiredNotDownloadedMessages(String mpc, Integer expiredNotDownloadedMessagesLimit) {
        LOG.debug("Deleting expired not-downloaded messages for MPC [{}] using expiredNotDownloadedMessagesLimit [{}]", mpc, expiredNotDownloadedMessagesLimit);
        final int messageRetentionNotDownloaded = pModeProvider.getRetentionUndownloadedByMpcURI(mpc);
        if (messageRetentionNotDownloaded > -1) { // if -1 the messages will be kept indefinitely and if 0, although it makes no sense, is legal
            final List<String> notDownloadedMessageIds = userMessageLogDao.getUndownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionNotDownloaded * -1),
                    mpc, expiredNotDownloadedMessagesLimit);
            if (CollectionUtils.isNotEmpty(notDownloadedMessageIds)) {
                final int deleted = notDownloadedMessageIds.size();
                LOG.debug("Found [{}] not-downloaded messages to delete", deleted);
                scheduleDeleteMessages(notDownloadedMessageIds);
                LOG.debug("Deleted [{}] not-downloaded messages", deleted);
            }
        }
    }

    @Override
    public void scheduleDeleteMessages(List<String> messageIds) {
        if(CollectionUtils.isEmpty(messageIds)) {
            LOG.debug("No message to be scheduled for deletion");
            return;
        }

        LOG.debug("Scheduling delete messages [{}]", messageIds);
        messageIds.forEach(messageId -> {
            JmsMessage message = JMSMessageBuilder.create()
                    .property(MessageConstants.MESSAGE_ID, messageId)
                    .build();
            jmsManager.sendMessageToQueue(message, retentionMessageQueue);
        });
    }

    protected Integer getRetentionValue(String propertyName) {
        return domibusPropertyProvider.getIntegerDomainProperty(propertyName);
    }

}
