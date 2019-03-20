package eu.domibus.ebms3.sender;

import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.RetryStrategy;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLogEntity;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class UpdateRetryLoggingService {

    public static final String DELETE_PAYLOAD_ON_SEND_FAILURE = "domibus.sendMessage.failure.delete.payload";
    public static final String MESSAGE_EXPIRATION_DELAY = "domibus.msh.retry.messageExpirationDelay";

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
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;


    /**
     * This method is responsible for the handling of retries for a given sent message.
     * In case of failure the message will be put back in waiting_for_retry status, after a certain amount of retry/time
     * it will be marked as failed.
     *
     * @param messageId        id of the message that needs to be retried
     * @param legConfiguration processing information for the message
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePushedMessageRetryLogging(final String messageId, final LegConfiguration legConfiguration) {
        updateRetryLogging(messageId, legConfiguration, MessageStatus.WAITING_FOR_RETRY);
    }


    private void updateRetryLogging(final String messageId, final LegConfiguration legConfiguration, MessageStatus messageStatus) {
        LOG.debug("Updating retry for message");
        UserMessageLogEntity userMessageLog = this.userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        userMessageLog.setSendAttempts(userMessageLog.getSendAttempts() + 1);
        LOG.debug("Updating sendAttempts to [{}]", userMessageLog.getSendAttempts());
        userMessageLog.setNextAttempt(getScheduledStartDate(userMessageLog)); // this is needed for the first computation of "next attempt" if receiver is down
        userMessageLogDao.update(userMessageLog);
        if (hasAttemptsLeft(userMessageLog, legConfiguration) && !userMessageLog.isTestMessage()) {
            updateNextAttemptAndNotify(legConfiguration, messageStatus, userMessageLog);
        } else { // max retries reached, mark message as ultimately failed (the message may be pushed back to the send queue by an administrator but this send completely failed)
            final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
            messageFailed(userMessage, userMessageLog);
        }
        uiReplicationSignalService.messageChange(userMessageLog.getMessageId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void messageFailedInANewTransaction(UserMessage userMessage, MessageLog userMessageLog) {
        messageFailed(userMessage, userMessageLog);
        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(userMessageLog.getMessageId());
    }

    public void messageFailed(UserMessage userMessage, MessageLog userMessageLog) {
        final String messageId = userMessageLog.getMessageId();
        LOG.businessError(DomibusMessageCode.BUS_MESSAGE_SEND_FAILURE);
        if (NotificationStatus.REQUIRED.equals(userMessageLog.getNotificationStatus()) && !userMessageLog.isTestMessage()) {
            LOG.info("Notifying backend for message failure");
            backendNotificationService.notifyOfSendFailure(userMessage);
        }
        userMessageLogService.setMessageAsSendFailure(messageId);

        if (domibusPropertyProvider.getBooleanDomainProperty(DELETE_PAYLOAD_ON_SEND_FAILURE)) {
            messagingDao.clearPayloadData(messageId);
        }
    }


    @Transactional
    public void updateWaitingReceiptMessageRetryLogging(final String messageId, final LegConfiguration legConfiguration) {
        LOG.debug("Updating waiting receipt retry for message");
        updateRetryLogging(messageId, legConfiguration, MessageStatus.WAITING_FOR_RECEIPT);
    }

    public void updateNextAttemptAndNotify(LegConfiguration legConfiguration, MessageStatus messageStatus, MessageLog userMessageLog) {
        updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
        saveAndNotify(messageStatus, userMessageLog);
    }

    public void saveAndNotify(MessageStatus messageStatus, MessageLog userMessageLog) {
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, messageStatus, new Timestamp(System.currentTimeMillis()));
        userMessageLog.setMessageStatus(messageStatus);
        LOG.debug("Updating status to [{}]", userMessageLog.getMessageStatus());
        userMessageLogDao.update(userMessageLog);

    }

    /**
     * Check if the message can be sent again: there is time and attempts left
     *
     * @param userMessageLog   the message to check
     * @param legConfiguration processing information for the message
     * @return true if the message can be sent again
     */
    public boolean hasAttemptsLeft(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
        if(legConfiguration.getReceptionAwareness() == null){
            return false;
        }
        LOG.debug("Send attempts [{}], max send attempts [{}], scheduled start time [{}], retry timeout [{}]",
                userMessageLog.getSendAttempts(), userMessageLog.getSendAttemptsMax(),
                getScheduledStartTime(userMessageLog), legConfiguration.getReceptionAwareness().getRetryTimeout());
        // retries start after the first send attempt
        Boolean hasMoreAttempts = userMessageLog.getSendAttempts() < userMessageLog.getSendAttemptsMax();
        long retryTimeout = legConfiguration.getReceptionAwareness().getRetryTimeout() * 60000L;
        Boolean hasMoreTime = (getScheduledStartTime(userMessageLog) + retryTimeout) > System.currentTimeMillis();

        LOG.debug("Verify if has more attempts: [{}] and has more time: [{}]", hasMoreAttempts, hasMoreTime);
        return hasMoreAttempts && hasMoreTime;
    }

    /**
     * Gets the scheduled start date of the message: if the message has been restored is returns the restored date otherwise it returns the received date
     *
     * @param userMessageLog the message
     * @return the scheduled start date in milliseconds elapsed since the UNIX epoch
     */
    public Long getScheduledStartTime(final MessageLog userMessageLog) {
        return getScheduledStartDate(userMessageLog).getTime();
    }

    public Date getScheduledStartDate(final MessageLog userMessageLog) {
        Date result = userMessageLog.getRestored();
        if (result == null) {
            LOG.debug("Using the received date for scheduled start time [{}]", userMessageLog.getReceived());
            return userMessageLog.getReceived();
        }
        return result;
    }

    public Date getMessageExpirationDate(final MessageLog userMessageLog,
                                         final LegConfiguration legConfiguration) {
        if (legConfiguration.getReceptionAwareness() != null) {
            final Long scheduledStartTime = getScheduledStartTime(userMessageLog);
            final int timeOut = legConfiguration.getReceptionAwareness().getRetryTimeout() * 60000;
            Date result = new Date(scheduledStartTime + timeOut);
            LOG.debug("Message expiration date is [{}]", result);
            return result;
        }
        return null;
    }

    public boolean isExpired(LegConfiguration legConfiguration, MessageLog userMessageLog) {
        int delay = domibusPropertyProvider.getIntegerProperty(MESSAGE_EXPIRATION_DELAY);
        Boolean isExpired =  (getMessageExpirationDate(userMessageLog, legConfiguration).getTime() + delay) < System.currentTimeMillis();
        LOG.debug("Verify if message expired: [{}]", isExpired);
        return isExpired;
    }

    public void updateMessageLogNextAttemptDate(LegConfiguration legConfiguration, MessageLog userMessageLog) {
        Date nextAttempt = new Date();
        if (userMessageLog.getNextAttempt() !=null) {
            nextAttempt = new Date(userMessageLog.getNextAttempt().getTime());
        }
        RetryStrategy.AttemptAlgorithm algorithm = legConfiguration.getReceptionAwareness().getStrategy().getAlgorithm();
        int retryCount = legConfiguration.getReceptionAwareness().getRetryCount();
        int retryTimeout = legConfiguration.getReceptionAwareness().getRetryTimeout();
        Date newNextAttempt = algorithm.compute(nextAttempt, retryCount, retryTimeout);
        LOG.debug("Updating next attempt from [{}] to [{}]", nextAttempt, newNextAttempt);
        userMessageLog.setNextAttempt(newNextAttempt);
    }
}


