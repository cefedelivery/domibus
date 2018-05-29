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
import eu.domibus.ebms3.common.dao.PModeProvider;
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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
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

    private static final String PULL_EXTRA_NUMBER_OF_ATTEMPT_TIME_FOR_EXPIRATION_DATE = "pull.extra.number.of.attempt.time.for.expiration.date";

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
    @Qualifier("domibusProperties")
    private java.util.Properties domibusProperties;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("domibusJDBC-XADataSource")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    private Integer extraNumberOfAttemptTimeForExpirationDate;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PullRequestResult updatePullMessageAfterRequest(final UserMessage userMessage,
                                                           final String messageId,
                                                           final LegConfiguration legConfiguration,
                                                           final ReliabilityChecker.CheckResult state) {
        UserMessageLog userMessageLog = this.userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        final int sendAttempts = userMessageLog.getSendAttempts() + 1;
        LOG.debug("[PULL_REQUEST]:Message[{}]:Increasing send attempts to[{}]", messageId, sendAttempts);
        userMessageLog.setSendAttempts(sendAttempts);
        switch (state) {
            case WAITING_FOR_CALLBACK:
                return waitingForCallBack(userMessage, legConfiguration, userMessageLog);
            case PULL_FAILED:
                return pullFailedOnRequest(userMessage, legConfiguration, userMessageLog);
            case ABORT:
                pullMessageStateService.sendFailed(userMessageLog);
                return new PullRequestResult(userMessageLog);
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
                return pullFailedOnReceipt(legConfiguration, userMessageLog, userMessage);

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
            PullMessageId pullMessageId = null;
            pullMessageId = messagingLockDao.getNextPullMessageToProcess(lock.getEntityId());
            if (pullMessageId != null) {
                //messagingLockDao.getLock(pullMessageId.getMessageId());
                LOG.debug("[PULL_REQUEST]:Message:[{}] retrieved", pullMessageId.getMessageId());
                final String messageId = pullMessageId.getMessageId();
                switch (pullMessageId.getState()) {
                    case EXPIRED:
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

        LOG.trace("[PULL_REQUEST]:Not message found.");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addPullMessageLock(final PartyIdExtractor partyIdExtractor, final UserMessage userMessage, final MessageLog messageLog) {
        MessagingLock messagingLock = prepareMessagingLock(partyIdExtractor, userMessage, messageLog);
        messagingLockDao.save(messagingLock);
    }

    private MessagingLock prepareMessagingLock(PartyIdExtractor partyIdExtractor, UserMessage userMessage, MessageLog messageLog) {
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
                messageLog.getNextAttempt(),
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


    @Override
    @Transactional
    public MessagingLock getLock(final String messageId) {
        return messagingLockDao.getLock(messageId);
    }

    protected Date getPullMessageExpirationDate(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
        if (legConfiguration.getReceptionAwareness() != null) {
            final Long scheduledStartTime = updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            final int timeOut = legConfiguration.getReceptionAwareness().getRetryTimeout() * 60000;
            long oneAttemptTimeInMillis = timeOut / legConfiguration.getReceptionAwareness().getRetryCount();
            return new Date(scheduledStartTime + timeOut + (getExtraNumberOfAttemptTimeForExpirationDate() * oneAttemptTimeInMillis));
        }
        return null;
    }

    protected int getExtraNumberOfAttemptTimeForExpirationDate() {
        if (extraNumberOfAttemptTimeForExpirationDate == null) {
            extraNumberOfAttemptTimeForExpirationDate = Integer.valueOf(domibusProperties.getProperty(PULL_EXTRA_NUMBER_OF_ATTEMPT_TIME_FOR_EXPIRATION_DATE, "2"));
        }
        return extraNumberOfAttemptTimeForExpirationDate;
    }

    protected void updateMessageLogNextAttemptDate(LegConfiguration legConfiguration, MessageLog userMessageLog) {
        final MessageLog userMessageLog1 = userMessageLog;
        Date nextAttempt = new Date();
        if (userMessageLog.getReceived().compareTo(userMessageLog.getNextAttempt()) < 0) {
            nextAttempt = userMessageLog.getNextAttempt();
        }
        userMessageLog1.setNextAttempt(legConfiguration.getReceptionAwareness().getStrategy().getAlgorithm().compute(nextAttempt, userMessageLog1.getSendAttemptsMax(), legConfiguration.getReceptionAwareness().getRetryTimeout()));
    }

    /**
     * This method is called when a message has been pulled successfully.
     *
     * @param userMessage
     * @param legConfiguration
     * @param userMessageLog
     */
    protected PullRequestResult waitingForCallBack(UserMessage userMessage, LegConfiguration legConfiguration, UserMessageLog userMessageLog) {
        if (isExpired(legConfiguration, userMessageLog)) {
            LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] expired]", userMessageLog.getMessageId());
            pullMessageStateService.sendFailed(userMessageLog);
            return new PullRequestResult(userMessageLog);
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
        userMessageLog.setMessageStatus(waitingForReceipt);
        userMessageLogDao.update(userMessageLog);
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, waitingForReceipt, new Timestamp(System.currentTimeMillis()));
        return new PullRequestResult(userMessageLog);
    }

    private boolean isExpired(LegConfiguration legConfiguration, MessageLog userMessageLog) {
        return getPullMessageExpirationDate(userMessageLog, legConfiguration).getTime() < System.currentTimeMillis();
    }

    /**
     * Check if the message can be sent again: there is time and attempts left
     */
    protected boolean attemptNumberLeftIsLowerOrEqualThenMaxAttempts(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
        // retries start after the first send attempt
        if (legConfiguration.getReceptionAwareness() != null && userMessageLog.getSendAttempts() <= userMessageLog.getSendAttemptsMax()
                && !isExpired(legConfiguration, userMessageLog)) {
            return true;
        }
        return false;
    }


    public boolean attemptNumberLeftIsStricltyLowerThenMaxAttemps(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
        // retries start after the first send attempt
        if (legConfiguration.getReceptionAwareness() != null && userMessageLog.getSendAttempts() < userMessageLog.getSendAttemptsMax()
                && !isExpired(legConfiguration, userMessageLog)) {
            return true;
        }
        return false;
    }

    protected PullRequestResult pullFailedOnRequest(UserMessage userMessage, LegConfiguration legConfiguration, UserMessageLog userMessageLog) {
        LOG.debug("[PULL_REQUEST]:Message:[{}] failed on pull message retrieval", userMessageLog.getMessageId());
        if (attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration)) {
            LOG.debug("[PULL_REQUEST]:Message:[{}] has been pulled [{}] times", userMessageLog.getMessageId(), userMessageLog.getSendAttempts() + 1);
            updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
            updateRetryLoggingService.saveAndNotify(MessageStatus.READY_TO_PULL, userMessageLog);
            LOG.debug("[pullFailedOnRequest]:Message:[{}] release lock", userMessageLog.getMessageId());
            LOG.debug("[PULL_REQUEST]:Message:[{}] will be available for pull at [{}]", userMessageLog.getMessageId(), userMessageLog.getNextAttempt());
        } else {
            LOG.debug("[PULL_REQUEST]:Message:[{}] has no more attempt, it has been pulled [{}] times", userMessageLog.getMessageId(), userMessageLog.getSendAttempts() + 1);
            pullMessageStateService.sendFailed(userMessageLog);
        }
        return new PullRequestResult(userMessageLog);
    }

    protected PullRequestResult pullFailedOnReceipt(LegConfiguration legConfiguration, UserMessageLog userMessageLog, UserMessage userMessage) {
        LOG.debug("[PULL_RECEIPT]:Message:[{}] failed on pull message acknowledgement", userMessageLog.getMessageId());

        if (attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration)) {
            LOG.debug("[PULL_RECEIPT]:Message:[{}] has been pulled [{}] times", userMessageLog.getMessageId(), userMessageLog.getSendAttempts() + 1);
            backendNotificationService.notifyOfMessageStatusChange(userMessageLog, MessageStatus.READY_TO_PULL, new Timestamp(System.currentTimeMillis()));
            pullMessageStateService.reset(userMessageLog);
            //releaseLock(userMessageLog);
            LOG.debug("[pullFailedOnReceipt]:Message:[{}] add lock", userMessageLog.getMessageId());
            LOG.debug("[PULL_RECEIPT]:Message:[{}] will be available for pull at [{}]", userMessageLog.getMessageId(), userMessageLog.getNextAttempt());

        } else {
            LOG.debug("[PULL_RECEIPT]:Message:[{}] has no more attempt, it has been pulled [{}] times", userMessageLog.getMessageId(), userMessageLog.getSendAttempts() + 1);
            pullMessageStateService.sendFailed(userMessageLog);
        }
        return new PullRequestResult(userMessageLog);

    }

    @Override
    public void readyToDelete(MessagingLock messagingLock) {
        messagingLock.setMessageState(MessageState.DEL);
        messagingLock.setNextAttempt(null);
        messagingLockDao.save(messagingLock);
    }

    @Override
    public void delete(final MessagingLock messagingLock) {
        messagingLockDao.delete(messagingLock);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteInNewTransaction(final MessagingLock messagingLock) {
        final MessagingLock lock = getLock(messagingLock.getMessageId());
        if (lock != null) {
            messagingLockDao.delete(lock);
        }
    }


    @Override
    @Transactional
    public void releaseLockAfterRequest(final PullRequestResult requestResult) {
        LOG.trace("[releaseLockAfterRequest]:release lock");
        final MessagingLock lock = messagingLockDao.findMessagingLockForMessageId(requestResult.getMessageId());
        switch (requestResult.getMessageStatus()) {
            case WAITING_FOR_RECEIPT:
                lock.setMessageState(MessageState.WAITING);
                lock.setSendAttempts(requestResult.getSendAttempts());
                lock.setNextAttempt(requestResult.getNextAttempts());
                break;
            case SEND_FAILURE:
                lock.setNextAttempt(null);
                lock.setMessageState(MessageState.DEL);
        }
        LOG.debug("[releaseLockAfterRequest]:Message:[{}] receive receiptResult  with status[{}] and next attempt:[{}].", requestResult.getMessageId(), requestResult.getMessageStatus(), requestResult.getNextAttempts());
        LOG.debug("[releaseLockAfterRequest]:Message:[{}] release lock  with status[{}] and next attempt:[{}].", lock.getMessageId(), lock.getMessageState(), lock.getNextAttempt());
        messagingLockDao.save(lock);
    }

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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetMessageInWaitingForReceptState(MessagingLock messagingLock) {
        final MessagingLock lock = getLock(messagingLock.getMessageId());
        if (lock == null) {
            LOG.debug("[bulkExpirePullMessages]:Message:[] lock cound not be acquired", messagingLock.getMessageId());
            return;
        }
        LOG.debug("[resetWaitingForReceiptPullMessages]:Message:[{}] checking if can be retry, attempts[{}], max attempts[{}], expiration date[{}]", messagingLock.getMessageId(), messagingLock.getSendAttempts(), messagingLock.getSendAttemptsMax(), messagingLock.getStaled());
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(lock.getMessageId());
        if (lock.getSendAttempts() < lock.getSendAttemptsMax() && lock.getStaled().getTime() > System.currentTimeMillis()) {
            LOG.debug("[resetWaitingForReceiptPullMessages]:Message:[{}] set ready for pulling", lock.getMessageId());
            pullMessageStateService.reset(userMessageLog);
            lock.setMessageState(MessageState.READY);
            messagingLockDao.save(lock);
            //notify ??
        } else {
            LOG.debug("[resetWaitingForReceiptPullMessages]:Message:[{}] send failed.", lock.getMessageId());
            lock.setMessageState(MessageState.DEL);
            lock.setNextAttempt(null);
            messagingLockDao.save(lock);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireMessage(MessagingLock staledMessage) {
        final MessagingLock lock = getLock(staledMessage.getMessageId());
        if (lock == null) {
            LOG.debug("[bulkExpirePullMessages]:Message:[] could not acquire lock", staledMessage.getMessageId());
            return;
        }
        LOG.debug("[bulkExpirePullMessages]:Message:[{}] expired.", lock.getMessageId());
        pullMessageStateService.sendFailed(userMessageLogDao.findByMessageId(lock.getMessageId()));
        delete(lock);
    }


}
