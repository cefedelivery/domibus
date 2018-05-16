package eu.domibus.common.services.impl;

import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.pull.MessagingLockService;
import eu.domibus.core.pull.ToExtractor;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.ebms3.sender.UpdateRetryLoggingService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

@Service
public class PullServiceImpl implements PullService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullServiceImpl.class);

    @Autowired
    private UserMessageLogService userMessageLogService;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private MessagingLockService messagingLockService;

    @Autowired
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void messageStaled(final String messageId){
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        sendFailed(userMessageLog);
    }

    private void sendFailed(UserMessageLog userMessageLog) {
        userMessageLog.setNextAttempt(null);
        updateRetryLoggingService.messageFailed(userMessageLog);
    }

    @Transactional
    public Date getStaledDate(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
        if (legConfiguration.getReceptionAwareness() != null) {
            final Long scheduledStartTime = updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            return new Date(scheduledStartTime + (legConfiguration.getReceptionAwareness().getRetryTimeout() * 60000));
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePullMessageAfterRequest(final UserMessage userMessage,
                                              final String messageId,
                                              final LegConfiguration legConfiguration,
                                              final ReliabilityChecker.CheckResult state) {
        UserMessageLog userMessageLog = this.userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        userMessageLog.setSendAttempts(userMessageLog.getSendAttempts() + 1);
        switch (state) {
            case WAITING_FOR_CALLBACK:
                waitingForCallBack(userMessage, legConfiguration, userMessageLog);
                break;
            case PULL_FAILED:
                pullFailedOnRequest(userMessage, legConfiguration, userMessageLog);
                break;
            default:
                throw new IllegalStateException(String.format("Status:[%s] should never occur here", state.name()));
        }
    }




    private void waitingForCallBack(UserMessage userMessage, LegConfiguration legConfiguration, UserMessageLog userMessageLog) {
        if (updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration)) {
            updateRetryLoggingService.increaseAttempAndNotify(legConfiguration, MessageStatus.WAITING_FOR_RECEIPT, userMessageLog);
        }
        messagingLockService.addSearchInFormation(new ToExtractor(userMessage.getPartyInfo().getTo()),userMessage,userMessageLog);
    }

    private void pullFailedOnRequest(UserMessage userMessage, LegConfiguration legConfiguration, UserMessageLog userMessageLog) {
        if (updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration)) {
            updateRetryLoggingService.increaseAttempAndNotify(legConfiguration, MessageStatus.READY_TO_PULL, userMessageLog);
            messagingLockService.addSearchInFormation(new ToExtractor(userMessage.getPartyInfo().getTo()),userMessage,userMessageLog);
        }
        else{
            sendFailed(userMessageLog);

        }
    }

    private void pullFailedOnReceipt(UserMessage userMessage, LegConfiguration legConfiguration, UserMessageLog userMessageLog) {
        if (updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration)) {
            backendNotificationService.notifyOfMessageStatusChange(userMessageLog, MessageStatus.READY_TO_PULL, new Timestamp(System.currentTimeMillis()));userMessageLog);
        }
        else{
            messagingLockService.delete(userMessageLog.getMessageId());
            sendFailed(userMessageLog);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePullMessageAfterReceipt(
            ReliabilityChecker.CheckResult reliabilityCheckSuccessful,
            ResponseHandler.CheckResult isOk,
            String messageId,
            UserMessage userMessage,
            LegConfiguration legConfiguration
            ){
        switch (reliabilityCheckSuccessful) {
            case OK:
                switch (isOk) {
                    case OK:
                        userMessageLogService.setMessageAsAcknowledged(messageId);
                        break;
                    case WARNING:
                        userMessageLogService.setMessageAsAckWithWarnings(messageId);
                        break;
                    default:
                        assert false;
                }
                backendNotificationService.notifyOfSendSuccess(messageId);
                messagingDao.clearPayloadData(messageId);
                LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SEND_SUCCESS, messageId);
                messagingLockService.delete(messageId);
                break;
            case PULL_FAILED:
                final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
                pullFailedOnReceipt(userMessage,legConfiguration,userMessageLog);
                break;
        }
    }

}
