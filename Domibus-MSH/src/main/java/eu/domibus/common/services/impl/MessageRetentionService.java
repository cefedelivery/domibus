package eu.domibus.common.services.impl;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.util.CollectionUtil;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This service class is responsible for the retention and clean up of Domibus messages, including signal messages.
 * Notice that only payloads data are really deleted.
 *
 * @author Christian Koch, Stefan Mueller, Federico Martini, Cosmin Baciu
 * @since 3.0
 */
@Service
public class MessageRetentionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageRetentionService.class);

    public static final Integer DEFAULT_DOWNLOADED_MESSAGES_DELETE_LIMIT = 50;
    public static final Integer DEFAULT_NOT_DOWNLOADED_MESSAGES_DELETE_LIMIT = 50;
    public static final String DOWNLOADED_MESSAGES_DELETE_LIMIT_PROPERTY = "message.retention.downloaded.max.delete";
    public static final String NOT_DOWNLOADED_MESSAGES_DELETE_LIMIT_PROPERTY = "message.retention.not_downloaded.max.delete";

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
    private UserMessageService userMessageService;


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
        final Integer expiredNotDownloadedMessagesLimit = Integer.MAX_VALUE;

        for (final String mpc : mpcs) {
            deleteExpiredMessages(mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit);
        }
    }

    @Transactional
    public void deleteExpiredMessages(String mpc, Integer expiredDownloadedMessagesLimit, Integer expiredNotDownloadedMessagesLimit) {
        LOG.debug("Deleting expired messages for MPC [" + mpc + "] using expiredDownloadedMessagesLimit [" + expiredDownloadedMessagesLimit + "]" +
                " and expiredNotDownloadedMessagesLimit [" + expiredNotDownloadedMessagesLimit + "]");
        deleteExpiredDownloadedMessages(mpc, expiredDownloadedMessagesLimit);
        deleteExpiredNotDownloadedMessages(mpc, expiredNotDownloadedMessagesLimit);
    }

    protected void deleteExpiredDownloadedMessages(String mpc, Integer expiredDownloadedMessagesLimit) {
        LOG.debug("Deleting expired downloaded messages for MPC [" + mpc + "] using expiredDownloadedMessagesLimit [" + expiredDownloadedMessagesLimit + "]");
        final int messageRetentionDownloaded = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
        String fileLocation = domibusProperties.getProperty("domibus.attachment.storage.location");
        // If messageRetentionDownloaded is equal to -1, the messages will be kept indefinitely and, if 0 and no file system storage was used, they have already been deleted during download operation.
        if (messageRetentionDownloaded > 0 || (StringUtils.isNotEmpty(fileLocation) && messageRetentionDownloaded >= 0)) {
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

    protected Integer delete(List<String> messageIds, Integer limit) {
        List<String> toDelete = messageIds;
        if (messageIds.size() > limit) {
            LOG.debug("Only the first [" + limit + "] will be deleted");
            toDelete = collectionUtil.safeSubList(messageIds, 0, limit);
        }
        userMessageService.delete(toDelete);
        return toDelete.size();
    }



}
