package eu.domibus.ebms3.sender;

import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

@Service
public class UpdateRetryLoggingService {

    private static final String DELETE_PAYLOAD_ON_SEND_FAILURE = "domibus.sendMessage.failure.delete.payload";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UpdateRetryLoggingService.class);

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageLogService userMessageLogService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    /**
     * This method is responsible for the handling of retries for a given sent message.
     * In case of failure the message will be put back in waiting_for_retry status, after a certain amount of retry/time
     * it will be marked as failed.
     *
     * @param messageId        id of the message that needs to be retried
     * @param legConfiguration processing information for the message
     */
    public void updatePushedMessageRetryLogging(final String messageId, final LegConfiguration legConfiguration) {
        updateRetryLogging(messageId, legConfiguration, MessageStatus.WAITING_FOR_RETRY);
    }

    /**
     * This method is responsible for the handling of retries for a given sent message.
     * In case of failure the message will be put back in READY_TO_PULL status, after a certain amount of retry/time
     * it will be marked as failed.
     *
     * @param messageId        id of the message that needs to be retried
     * @param legConfiguration processing information for the message
     */

    public void updatePulledMessageRetryLogging(final String messageId, final LegConfiguration legConfiguration) {
        updateRetryLogging(messageId, legConfiguration, MessageStatus.READY_TO_PULL);
    }

    private void updateRetryLogging(final String messageId, final LegConfiguration legConfiguration, MessageStatus messageStatus) {
        LOG.debug("Updating retry for message");
        MessageLog userMessageLog = this.userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        userMessageLog.setSendAttempts(userMessageLog.getSendAttempts() + 1);
        LOG.debug("Updating sendAttempts to [{}]", userMessageLog.getSendAttempts());
        userMessageLogDao.update(userMessageLog);
        if (hasAttemptsLeft(userMessageLog, legConfiguration)) {
            LOG.debug("Updating send attempts to [{}]", userMessageLog.getSendAttempts());
            if (legConfiguration.getReceptionAwareness() != null) {
                userMessageLog.setNextAttempt(legConfiguration.getReceptionAwareness().getStrategy().getAlgorithm().compute(userMessageLog.getNextAttempt(), userMessageLog.getSendAttemptsMax(), legConfiguration.getReceptionAwareness().getRetryTimeout()));
                backendNotificationService.notifyOfMessageStatusChange(userMessageLog, messageStatus, new Timestamp(System.currentTimeMillis()));
                userMessageLog.setMessageStatus(messageStatus);
                LOG.debug("Updating status to [{}]", userMessageLog.getMessageStatus());
                userMessageLogDao.update(userMessageLog);
            }

        } else { // max retries reached, mark message as ultimately failed (the message may be pushed back to the send queue by an administrator but this send completely failed)
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_SEND_FAILURE);
            if (NotificationStatus.REQUIRED.equals(userMessageLog.getNotificationStatus())) {
                LOG.debug("Notifying backend for message failure");
                backendNotificationService.notifyOfSendFailure(messageId);
            }
            userMessageLogService.setMessageAsSendFailure(messageId);

            if ("true".equals(domibusProperties.getProperty(DELETE_PAYLOAD_ON_SEND_FAILURE, "false"))) {
                messagingDao.clearPayloadData(messageId);
            }
        }
    }

    /**
     * Check if the message can be sent again: there is time and attempts left
     */
    protected boolean hasAttemptsLeft(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
        // retries start after the first send attempt
        if (userMessageLog.getSendAttempts() < userMessageLog.getSendAttemptsMax()
                && (getScheduledStartTime(userMessageLog) + legConfiguration.getReceptionAwareness().getRetryTimeout() * 60000) > System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    /**
     * Gets the scheduled start date of the message: if the message has been restored is returns the restored date otherwise it returns the received date
     *
     * @param userMessageLog
     * @return
     */
    protected Long getScheduledStartTime(final MessageLog userMessageLog) {
        Date result = userMessageLog.getRestored();
        if (result == null) {
            LOG.debug("Using the received date for scheduled start time [{}]", userMessageLog.getReceived());
            return userMessageLog.getReceived().getTime();
        }
        return result.getTime();
    }

}
