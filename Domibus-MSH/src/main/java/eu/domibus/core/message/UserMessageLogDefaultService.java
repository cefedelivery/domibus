package eu.domibus.core.message;

import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.model.logging.UserMessageLogBuilder;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import eu.domibus.ebms3.common.model.MessageSubtype;
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

    @Autowired
    protected UIReplicationSignalService uiReplicationSignalService;

    private UserMessageLog createUserMessageLog(String messageId, String messageStatus, String notificationStatus, String mshRole, Integer maxAttempts, String mpc, String backendName, String endpoint) {
        // Builds the user message log
        UserMessageLogBuilder umlBuilder = UserMessageLogBuilder.create()
                .setMessageId(messageId)
                .setMessageStatus(MessageStatus.valueOf(messageStatus))
                .setMshRole(MSHRole.valueOf(mshRole))
                .setNotificationStatus(NotificationStatus.valueOf(notificationStatus))
                .setMpc(mpc)
                .setSendAttemptsMax(maxAttempts)
                .setBackendName(backendName)
                .setEndpoint(endpoint);

        return umlBuilder.build();
    }

    @Override
    public void save(String messageId, String messageStatus, String notificationStatus, String mshRole, Integer maxAttempts, String mpc, String backendName, String endpoint) {
        final MessageStatus status = MessageStatus.valueOf(messageStatus);
        // Builds the user message log
        final UserMessageLog userMessageLog = createUserMessageLog(messageId, messageStatus, notificationStatus, mshRole, maxAttempts, mpc, backendName, endpoint);

        //we set the status after we send the status change event; otherwise the old status and the new status would be the same
        userMessageLog.setMessageStatus(status);
        userMessageLogDao.create(userMessageLog);
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, status, new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public void save(String messageId, String messageStatus, String notificationStatus, String mshRole, Integer maxAttempts, String mpc, String backendName, String endpoint, String service, String action) {
        final MessageStatus status = MessageStatus.valueOf(messageStatus);
        // Builds the user message log
        final UserMessageLog userMessageLog = createUserMessageLog(messageId, messageStatus, notificationStatus, mshRole, maxAttempts, mpc, backendName, endpoint);
        // Sets the subtype
        MessageSubtype messageSubtype = null;
        if (checkTestMessage(service, action)) {
            messageSubtype = MessageSubtype.TEST;
        }
        userMessageLog.setMessageSubtype(messageSubtype);
        if (!MessageSubtype.TEST.equals(messageSubtype)) {
            backendNotificationService.notifyOfMessageStatusChange(userMessageLog, status, new Timestamp(System.currentTimeMillis()));
        }
        //we set the status after we send the status change event; otherwise the old status and the new status would be the same
        userMessageLog.setMessageStatus(status);
        userMessageLogDao.create(userMessageLog);
    }

    protected void updateMessageStatus(final String messageId, final MessageStatus newStatus) {
        final UserMessageLog messageLog = userMessageLogDao.findByMessageId(messageId);
        if (MessageType.USER_MESSAGE == messageLog.getMessageType() && !messageLog.isTestMessage()) {
            backendNotificationService.notifyOfMessageStatusChange(messageLog, newStatus, new Timestamp(System.currentTimeMillis()));
        }
        userMessageLogDao.setMessageStatus(messageLog, newStatus);

        uiReplicationSignalService.messageStatusChange(messageId, newStatus);
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

    /**
     * Checks <code>service</code> and <code>action</code> to determine if it's a TEST message
     *
     * @param service Service
     * @param action  Action
     * @return True, if it's a test message and false otherwise
     */
    protected Boolean checkTestMessage(final String service, final String action) {
        return Ebms3Constants.TEST_SERVICE.equalsIgnoreCase(service)
                && Ebms3Constants.TEST_ACTION.equalsIgnoreCase(action);

    }
}
