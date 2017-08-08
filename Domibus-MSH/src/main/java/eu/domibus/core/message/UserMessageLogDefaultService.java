package eu.domibus.core.message;

import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.model.logging.UserMessageLogBuilder;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class UserMessageLogDefaultService implements UserMessageLogService {

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    BackendNotificationService backendNotificationService;

    @Override
    public void save(String messageId, String messageStatus, String notificationStatus, String mshRole, Integer maxAttempts, String mpc, String backendName, String endpoint) {
        // Builds the user message log
        final MessageStatus status = MessageStatus.valueOf(messageStatus);
        UserMessageLogBuilder umlBuilder = UserMessageLogBuilder.create()
                .setMessageId(messageId)
                .setMshRole(MSHRole.valueOf(mshRole))
                .setNotificationStatus(NotificationStatus.valueOf(notificationStatus))
                .setMpc(mpc)
                .setSendAttemptsMax(maxAttempts)
                .setBackendName(backendName)
                .setEndpoint(endpoint);

        final UserMessageLog userMessageLog = umlBuilder.build();
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, status, new Timestamp(System.currentTimeMillis()));
        //we set the status after we send the status change event; otherwise the old status and the new status would be the same
        userMessageLog.setMessageStatus(status);
        userMessageLogDao.create(userMessageLog);

    }

    protected void updateMessageStatus(final String messageId, final MessageStatus newStatus) {
        final UserMessageLog messageLog = userMessageLogDao.findByMessageId(messageId);
        if (MessageType.USER_MESSAGE == messageLog.getMessageType()) {
            backendNotificationService.notifyOfMessageStatusChange(messageLog, newStatus, new Timestamp(System.currentTimeMillis()));
        }
        userMessageLogDao.setMessageStatus(messageLog, newStatus);
    }

    @Override
    public void setMessageAsDeleted(String messageId) {
        updateMessageStatus(messageId, MessageStatus.DELETED);
    }

    @Override
    public void setMessageAsDownloaded(String messageId) {
        updateMessageStatus(messageId, MessageStatus.DOWNLOADED);
    }

    @Override
    public void setMessageAsAcknowledged(String messageId) {
        updateMessageStatus(messageId, MessageStatus.ACKNOWLEDGED);
    }

    @Override
    public void setMessageAsAckWithWarnings(String messageId) {
        updateMessageStatus(messageId, MessageStatus.ACKNOWLEDGED_WITH_WARNING);
    }

    @Override
    public void setMessageAsWaitingForReceipt(String messageId) {
        updateMessageStatus(messageId, MessageStatus.WAITING_FOR_RECEIPT);
    }

    @Override
    public void setMessageAsSendFailure(String messageId) {
        updateMessageStatus(messageId, MessageStatus.SEND_FAILURE);
    }

    @Override
    public void setIntermediaryPullStatus(String messageId) {
        updateMessageStatus(messageId, MessageStatus.BEING_PULLED);
    }
}
