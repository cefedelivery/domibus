package eu.domibus.ebms3.sender;

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
    private MessagingDao messagingDao;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    /**
     * This method is responsible for the handling of retries for a given message
     *
     * @param messageId        id of the message that needs to be retried
     * @param legConfiguration processing information for the message
     */
    public void updateRetryLogging(final String messageId, final LegConfiguration legConfiguration) {
        LOG.debug("Updating retry for message");
        final MessageLog userMessageLog = this.userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        //userMessageLog.setMessageStatus(MessageStatus.SEND_ATTEMPT_FAILED); //This is not stored in the database
        if (hasAttemptsLeft(userMessageLog, legConfiguration)) {
            userMessageLog.setSendAttempts(userMessageLog.getSendAttempts() + 1);
            LOG.debug("Updating send attempts to [{}]", userMessageLog.getSendAttempts());
            if (legConfiguration.getReceptionAwareness() != null) {
                userMessageLog.setNextAttempt(legConfiguration.getReceptionAwareness().getStrategy().getAlgorithm().compute(userMessageLog.getNextAttempt(), userMessageLog.getSendAttemptsMax(), legConfiguration.getReceptionAwareness().getRetryTimeout()));
                userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);
                LOG.debug("Updating status to [{}]", userMessageLog.getMessageStatus());
                userMessageLogDao.update(userMessageLog);
            }

        } else { // max retries reached, mark message as ultimately failed (the message may be pushed back to the send queue by an administrator but this send completely failed)
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_SEND_FAILURE);
            if (NotificationStatus.REQUIRED.equals(userMessageLog.getNotificationStatus())) {
                LOG.debug("Notifying backend for message failure");
                backendNotificationService.notifyOfSendFailure(messageId);
                userMessageLogDao.setAsNotified(messageId);
            }
            userMessageLogDao.setMessageAsSendFailure(messageId);

            if ("true".equals(domibusProperties.getProperty(DELETE_PAYLOAD_ON_SEND_FAILURE, "false"))) {
                messagingDao.clearPayloadData(messageId);
            }
        }
    }

    /**
     * Check if the message can be sent again: there is time and attempts left
     */
    protected boolean hasAttemptsLeft(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
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
        Date result =  userMessageLog.getRestored();
        if(result == null) {
            LOG.debug("Using the received date for scheduled start time [{}]", userMessageLog.getReceived());
            return userMessageLog.getReceived().getTime();
        }
        return result.getTime();
    }

}
