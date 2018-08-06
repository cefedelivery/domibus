package eu.domibus.core.pull;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.ebms3.sender.UpdateRetryLoggingService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class PullMessageServiceImpl implements PullMessageService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullMessageServiceImpl.class);

    public static final String MPC = "mpc";

    private static final String INITIATOR = "initiator";

    public static final String MESSAGE_TYPE = "messageType";

    private static final String CURRENT_TIME = "current_time";

    protected static final String PULL_EXTRA_NUMBER_OF_ATTEMPT_TIME_FOR_EXPIRATION_DATE = "pull.extra.number.of.attempt.time.for.expiration.date";

    @Autowired
    private UserMessageLogService userMessageLogService;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private PullMessageStateService pullMessageStateService;

    @Autowired
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    private MessagingLockDao messagingLockDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    @Qualifier("domibusProperties")
    private java.util.Properties domibusProperties;

    private Integer extraNumberOfAttemptTimeForExpirationDate;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePullMessageAfterRequest(final UserMessage userMessage,
                                              final String messageId,
                                              final LegConfiguration legConfiguration,
                                              final ReliabilityChecker.CheckResult state) {

        final MessagingLock lock = getLock(messageId);
        if (lock == null || MessageState.PROCESS != lock.getMessageState()) {
            LOG.warn("Message[] could not acquire lock when updating status, it has been handled by another process.", messageId);
            return;
        }


        UserMessageLog userMessageLog = this.userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        final int sendAttempts = userMessageLog.getSendAttempts() + 1;
        LOG.debug("[PULL_REQUEST]:Message[{}]:Increasing send attempts to[{}]", messageId, sendAttempts);
        userMessageLog.setSendAttempts(sendAttempts);
        switch (state) {
            case WAITING_FOR_CALLBACK:
                waitingForCallBack(legConfiguration, userMessageLog);
                break;
            case PULL_FAILED:
                pullFailedOnRequest(legConfiguration, userMessageLog);
                break;
            case ABORT:
                pullMessageStateService.sendFailed(userMessageLog);
                lock.setNextAttempt(null);
                lock.setMessageState(MessageState.DEL);
                messagingLockDao.save(lock);
                break;
            default:
                throw new IllegalStateException(String.format("Status:[%s] should never occur here", state.name()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PullRequestResult updatePullMessageAfterReceipt(
            ReliabilityChecker.CheckResult reliabilityCheckSuccessful,
            ResponseHandler.CheckResult isOk,
            UserMessageLog userMessageLog,
            LegConfiguration legConfiguration,
            UserMessage userMessage) {
        final String messageId = userMessageLog.getMessageId();
        LOG.debug("[releaseLockAfterReceipt]:Message:[{}] release lock]", messageId);

        switch (reliabilityCheckSuccessful) {
            case OK:
                switch (isOk) {
                    case OK:
                        userMessageLogService.setMessageAsAcknowledged(messageId);
                        LOG.debug("[PULL_RECEIPT]:Message:[{}] acknowledged.", messageId);
                        break;
                    case WARNING:
                        userMessageLogService.setMessageAsAckWithWarnings(messageId);
                        LOG.debug("[PULL_RECEIPT]:Message:[{}] acknowledged with warning.", messageId);
                        break;
                    default:
                        assert false;
                }
                backendNotificationService.notifyOfSendSuccess(messageId);
                LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SEND_SUCCESS, messageId);
                messagingDao.clearPayloadData(messageId);
                userMessageLog.setMessageStatus(MessageStatus.ACKNOWLEDGED);
                return new PullRequestResult(userMessageLog);
            case PULL_FAILED:
                return pullFailedOnReceipt(legConfiguration, userMessageLog);

        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String getPullMessageId(final String initiator, final String mpc) {
        final List<MessagingLock> messagingLock = messagingLockDao.findReadyToPull(mpc, initiator);
        LOG.trace("[PULL_REQUEST]:Reading messages for initiatior [{}] mpc[{}].", initiator, mpc);
        for (MessagingLock lock : messagingLock) {
            LOG.trace("[getPullMessageId]:Message[{}]] try to acquire lock", lock.getMessageId());
            PullMessageId pullMessageId = null;
            try {
                pullMessageId = messagingLockDao.getNextPullMessageToProcess(lock.getEntityId());
            } catch (Exception ex) {
                LOG.error("Error while locking message ", ex);
            }
            if (pullMessageId != null) {
                LOG.debug("[PULL_REQUEST]:Message:[{}] retrieved", pullMessageId.getMessageId());
                final String messageId = pullMessageId.getMessageId();
                switch (pullMessageId.getState()) {
                    case EXPIRED:
                        pullMessageStateService.expirePullMessage(messageId);
                        LOG.debug("[PULL_REQUEST]:Message:[{}] is staled for reason:[{}].", pullMessageId.getMessageId(), pullMessageId.getStaledReason());
                        break;
                    case FIRST_ATTEMPT:
                        LOG.debug("[PULL_REQUEST]:Message:[{}] first pull attempt.", pullMessageId.getMessageId());
                        return messageId;
                    case RETRY:
                        LOG.debug("[PULL_REQUEST]:message:[{}] retry pull attempt.", pullMessageId.getMessageId());
                        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(messageId);
                        return messageId;
                }
            }
        }
        LOG.trace("[PULL_REQUEST]:No message found.");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addPullMessageLock(final PartyIdExtractor partyIdExtractor, final UserMessage userMessage,
                                   final MessageLog messageLog) {
        MessagingLock messagingLock = prepareMessagingLock(partyIdExtractor, userMessage, messageLog);
        messagingLockDao.save(messagingLock);
    }

    private MessagingLock prepareMessagingLock(PartyIdExtractor partyIdExtractor, UserMessage
            userMessage, MessageLog messageLog) {
        String partyId = partyIdExtractor.getPartyId();
        final String messageId = messageLog.getMessageId();
        final String mpc = messageLog.getMpc();
        LOG.trace("Saving message lock with partyID:[{}], mpc:[{}]", partyId, mpc);
        final String pmodeKey; // FIXME: This does not work for signalmessages
        try {
            pmodeKey = this.pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
        } catch (EbMS3Exception e) {
            throw new PModeException(DomibusCoreErrorCode.DOM_001, "Could not get the PMode key for message [" + messageId + "]", e);
        }
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final Date staledDate = getPullMessageExpirationDate(messageLog, legConfiguration);

        return new MessagingLock(
                messageId,
                partyId,
                mpc,
                messageLog.getReceived(),
                staledDate,
                messageLog.getNextAttempt()==null?new Date():messageLog.getNextAttempt(),
                messageLog.getSendAttempts(),
                messageLog.getSendAttemptsMax());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deletePullMessageLock(final String messageId) {
        messagingLockDao.delete(messageId);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessagingLock getLock(final String messageId) {
        return messagingLockDao.getLock(messageId);
    }

    protected Date getPullMessageExpirationDate(final MessageLog userMessageLog,
                                                final LegConfiguration legConfiguration) {
        if (legConfiguration.getReceptionAwareness() != null) {
            final Long scheduledStartTime = updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            final int timeOut = legConfiguration.getReceptionAwareness().getRetryTimeout() * 60000;
            return new Date(scheduledStartTime + timeOut);
        }
        return null;
    }

    protected void updateMessageLogNextAttemptDate(LegConfiguration legConfiguration, MessageLog userMessageLog) {
        final MessageLog userMessageLog1 = userMessageLog;
        Date nextAttempt = new Date();
        if (userMessageLog.getNextAttempt() !=null) {
            nextAttempt = userMessageLog.getNextAttempt();
        }
        userMessageLog1.setNextAttempt(legConfiguration.getReceptionAwareness().getStrategy().getAlgorithm().compute(nextAttempt, userMessageLog1.getSendAttemptsMax(), legConfiguration.getReceptionAwareness().getRetryTimeout()));
    }

    /**
     * This method is called when a message has been pulled successfully.
     *  @param legConfiguration
     * @param userMessageLog
     */
    protected void waitingForCallBack(LegConfiguration legConfiguration, UserMessageLog
            userMessageLog) {
        final MessagingLock lock = messagingLockDao.findMessagingLockForMessageId(userMessageLog.getMessageId());
        if (isExpired(legConfiguration, userMessageLog)) {
            LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] expired]", userMessageLog.getMessageId());
            pullMessageStateService.sendFailed(userMessageLog);
            lock.setNextAttempt(null);
            lock.setMessageState(MessageState.DEL);
            messagingLockDao.save(lock);
            return;
        }
        final MessageStatus waitingForReceipt = MessageStatus.WAITING_FOR_RECEIPT;
        LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] change status to:[{}]", userMessageLog.getMessageId(), waitingForReceipt);
        updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
        if (LOG.isDebugEnabled()) {
            if (attemptNumberLeftIsLowerOrEqualThenMaxAttempts(userMessageLog, legConfiguration)) {
                LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] has been pulled [{}] times", userMessageLog.getMessageId(), userMessageLog.getSendAttempts());
                LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] In case of failure, will be available for pull at [{}]", userMessageLog.getMessageId(), userMessageLog.getNextAttempt());
            } else {
                LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] has no more attempt, it has been pulled [{}] times and it will be the last try.", userMessageLog.getMessageId(), userMessageLog.getSendAttempts());
            }
        }
        lock.setMessageState(MessageState.WAITING);
        lock.setSendAttempts(userMessageLog.getSendAttempts());
        lock.setNextAttempt(userMessageLog.getNextAttempt());
        userMessageLog.setMessageStatus(waitingForReceipt);
        messagingLockDao.save(lock);
        userMessageLogDao.update(userMessageLog);
        uiReplicationSignalService.messageStatusChange(userMessageLog.getMessageId());
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, waitingForReceipt, new Timestamp(System.currentTimeMillis()));
    }

    protected boolean isExpired(LegConfiguration legConfiguration, MessageLog userMessageLog) {
        return getPullMessageExpirationDate(userMessageLog, legConfiguration).getTime() < System.currentTimeMillis();
    }

    /**
     * Check if the message can be sent again: there is time and attempts left
     */
    protected boolean attemptNumberLeftIsLowerOrEqualThenMaxAttempts(final MessageLog userMessageLog,
                                                                     final LegConfiguration legConfiguration) {
        // retries start after the first send attempt
        if (legConfiguration.getReceptionAwareness() != null && userMessageLog.getSendAttempts() <= userMessageLog.getSendAttemptsMax()
                && !isExpired(legConfiguration, userMessageLog)) {
            return true;
        }
        return false;
    }


    protected boolean attemptNumberLeftIsStricltyLowerThenMaxAttemps(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
        // retries start after the first send attempt
        if (legConfiguration.getReceptionAwareness() != null && userMessageLog.getSendAttempts() < userMessageLog.getSendAttemptsMax()
                && !isExpired(legConfiguration, userMessageLog)) {
            return true;
        }
        return false;
    }

    protected void pullFailedOnRequest(LegConfiguration legConfiguration, UserMessageLog
            userMessageLog) {
        LOG.debug("[PULL_REQUEST]:Message:[{}] failed on pull message retrieval", userMessageLog.getMessageId());
        final MessagingLock lock = messagingLockDao.findMessagingLockForMessageId(userMessageLog.getMessageId());
        if (attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration)) {
            LOG.debug("[PULL_REQUEST]:Message:[{}] has been pulled [{}] times", userMessageLog.getMessageId(), userMessageLog.getSendAttempts() + 1);
            updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
            updateRetryLoggingService.saveAndNotify(MessageStatus.READY_TO_PULL, userMessageLog);
            LOG.debug("[pullFailedOnRequest]:Message:[{}] release lock", userMessageLog.getMessageId());
            LOG.debug("[PULL_REQUEST]:Message:[{}] will be available for pull at [{}]", userMessageLog.getMessageId(), userMessageLog.getNextAttempt());
            lock.setMessageState(MessageState.READY);
            lock.setSendAttempts(userMessageLog.getSendAttempts());
            lock.setNextAttempt(userMessageLog.getNextAttempt());
        } else {
            lock.setNextAttempt(null);
            lock.setMessageState(MessageState.DEL);
            LOG.debug("[PULL_REQUEST]:Message:[{}] has no more attempt, it has been pulled [{}] times", userMessageLog.getMessageId(), userMessageLog.getSendAttempts() + 1);
            pullMessageStateService.sendFailed(userMessageLog);
        }
        messagingLockDao.save(lock);
    }

    protected PullRequestResult pullFailedOnReceipt(LegConfiguration legConfiguration, UserMessageLog
            userMessageLog) {
        LOG.debug("[PULL_RECEIPT]:Message:[{}] failed on pull message acknowledgement", userMessageLog.getMessageId());
        if (attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration)) {
            LOG.debug("[PULL_RECEIPT]:Message:[{}] has been pulled [{}] times", userMessageLog.getMessageId(), userMessageLog.getSendAttempts() + 1);
            pullMessageStateService.reset(userMessageLog);
            LOG.debug("[pullFailedOnReceipt]:Message:[{}] add lock", userMessageLog.getMessageId());
            LOG.debug("[PULL_RECEIPT]:Message:[{}] will be available for pull at [{}]", userMessageLog.getMessageId(), userMessageLog.getNextAttempt());

        } else {
            LOG.debug("[PULL_RECEIPT]:Message:[{}] has no more attempt, it has been pulled [{}] times", userMessageLog.getMessageId(), userMessageLog.getSendAttempts() + 1);
            pullMessageStateService.sendFailed(userMessageLog);
        }
        return new PullRequestResult(userMessageLog);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final MessagingLock messagingLock) {
        messagingLockDao.delete(messagingLock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteInNewTransaction(final String messageId) {
        final MessagingLock lock = getLock(messageId);
        if (lock != null) {
            messagingLockDao.delete(lock);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetMessageInWaitingForReceiptState(final String messageId) {
        MessagingLock lock = getLock(messageId);
        if (lock == null) {
            return;
        }
        LOG.debug("[resetWaitingForReceiptPullMessages]:Message:[{}] checking if can be retry, attempts[{}], max attempts[{}], expiration date[{}]", lock.getMessageId(), lock.getSendAttempts(), lock.getSendAttemptsMax(), lock.getStaled());
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(lock.getMessageId());
        if (lock.getSendAttempts() < lock.getSendAttemptsMax() && lock.getStaled().getTime() > System.currentTimeMillis()) {
            LOG.debug("[resetWaitingForReceiptPullMessages]:Message:[{}] set ready for pulling", lock.getMessageId());
            pullMessageStateService.reset(userMessageLog);
            lock.setMessageState(MessageState.READY);
            messagingLockDao.save(lock);
        } else {
            LOG.debug("[resetWaitingForReceiptPullMessages]:Message:[{}] send failed.", lock.getMessageId());
            lock.setMessageState(MessageState.DEL);
            lock.setNextAttempt(null);
            messagingLockDao.save(lock);
            pullMessageStateService.sendFailed(userMessageLog);

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireMessage(String messageId) {
        MessagingLock lock = messagingLockDao.getLock(messageId);
        if (lock != null && MessageState.ACK != lock.getMessageState()) {
            LOG.debug("[bulkExpirePullMessages]:Message:[{}] expired.", lock.getMessageId());
            pullMessageStateService.sendFailed(userMessageLogDao.findByMessageId(lock.getMessageId()));
            delete(lock);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseLockAfterReceipt(final PullRequestResult requestResult) {
        LOG.debug("[releaseLockAfterReceipt]:Message:[{}] release lock]", requestResult.getMessageId());
        final MessagingLock lock = messagingLockDao.findMessagingLockForMessageId(requestResult.getMessageId());
        switch (requestResult.getMessageStatus()) {
            case READY_TO_PULL:
                lock.setMessageState(MessageState.READY);
                lock.setSendAttempts(requestResult.getSendAttempts());
                lock.setNextAttempt(requestResult.getNextAttempts());
                messagingLockDao.save(lock);
                break;
            case SEND_FAILURE:
                lock.setMessageState(MessageState.DEL);
                lock.setNextAttempt(null);
                messagingLockDao.save(lock);
                break;
            case ACKNOWLEDGED:
                lock.setNextAttempt(null);
                lock.setMessageState(MessageState.ACK);
                messagingLockDao.delete(lock);
                break;
        }
        LOG.debug("[releaseLockAfterReceipt]:Message:[{}] receive receiptResult  with status[{}] and next attempt:[{}].", requestResult.getMessageId(), requestResult.getMessageStatus(), requestResult.getNextAttempts());
        LOG.debug("[releaseLockAfterReceipt]:Message:[{}] release lock  with status[{}] and next attempt:[{}].", lock.getMessageId(), lock.getMessageState(), lock.getNextAttempt());

    }

}
