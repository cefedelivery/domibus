package eu.domibus.common.services.impl;

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
import eu.domibus.core.pull.*;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.ebms3.common.model.To;
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
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PullMessageServiceImpl implements PullMessageService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullMessageServiceImpl.class);

    public static final String MPC = "mpc";

    public static final String INITIATOR = "initiator";

    public static final String MESSAGE_TYPE = "messageType";

    public static final String CURRENT_TIME = "current_time";

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

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("domibusJDBC-XADataSource")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }


    private Date getStaledDate(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
        if (legConfiguration.getReceptionAwareness() != null) {
            final Long scheduledStartTime = updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            return new Date(scheduledStartTime + (legConfiguration.getReceptionAwareness().getRetryTimeout() * 60000));
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
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


    /**
     * This method is called when a message has been pulled successfully.
     *
     * @param userMessage
     * @param legConfiguration
     * @param userMessageLog
     */
    private void waitingForCallBack(UserMessage userMessage, LegConfiguration legConfiguration, UserMessageLog userMessageLog) {
        if (updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration)) {
            userMessageLog.setNextAttempt(legConfiguration.getReceptionAwareness().getStrategy().getAlgorithm().compute(userMessageLog.getNextAttempt(), userMessageLog.getSendAttemptsMax(), legConfiguration.getReceptionAwareness().getRetryTimeout()));

        } else {
            rawEnvelopeLogDao.deleteUserMessageRawEnvelope(userMessageLog.getMessageId());
        }
        final MessageStatus waitingForReceipt = MessageStatus.WAITING_FOR_RECEIPT;
        userMessageLog.setMessageStatus(waitingForReceipt);
        LOG.debug("Updating status to [{}]", userMessageLog.getMessageStatus());
        userMessageLogDao.update(userMessageLog);
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, waitingForReceipt, new Timestamp(System.currentTimeMillis()));
        addPullMessageLock(new ToExtractor(userMessage.getPartyInfo().getTo()), userMessage, userMessageLog);
    }

    private void pullFailedOnRequest(UserMessage userMessage, LegConfiguration legConfiguration, UserMessageLog userMessageLog) {
        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(userMessageLog.getMessageId());
        if (updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration)) {
            updateRetryLoggingService.increaseAttempAndNotify(legConfiguration, MessageStatus.READY_TO_PULL, userMessageLog);
            addPullMessageLock(new ToExtractor(userMessage.getPartyInfo().getTo()), userMessage, userMessageLog);
        } else {
            pullMessageStateService.sendFailed(userMessageLog);

        }
    }

    private void pullFailedOnReceipt(LegConfiguration legConfiguration, UserMessageLog userMessageLog) {
        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(userMessageLog.getMessageId());
        if (updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration)) {
            backendNotificationService.notifyOfMessageStatusChange(userMessageLog, MessageStatus.READY_TO_PULL, new Timestamp(System.currentTimeMillis()));
        } else {
            deletePullMessageLock(userMessageLog.getMessageId());
            pullMessageStateService.sendFailed(userMessageLog);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePullMessageAfterReceipt(
            ReliabilityChecker.CheckResult reliabilityCheckSuccessful,
            ResponseHandler.CheckResult isOk,
            String messageId,
            UserMessage userMessage,
            LegConfiguration legConfiguration
    ) {
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
                deletePullMessageLock(messageId);
                break;
            case PULL_FAILED:
                final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
                pullFailedOnReceipt(legConfiguration, userMessageLog);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String getPullMessageId(final String initiator, final String mpc) {
        Map<String, Object> params = new HashMap<>();
        params.put(MPC, mpc);
        params.put(INITIATOR, initiator);
        params.put(MESSAGE_TYPE, MessagingLock.PULL);
        params.put(CURRENT_TIME, new Date(System.currentTimeMillis()));
        final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select ID_PK from TB_MESSAGING_LOCK where MESSAGE_STATE = 'READY' and MPC=:mpc and INITIATOR=:initiator AND message_type=:messageType AND (NEXT_ATTEMPT is null  or NEXT_ATTEMPT<:current_time) order by ID_PK", params);
        LOG.debug("Reading messages for initiatior [{}] mpc[{}]", initiator, mpc);
        while (sqlRowSet.next()) {
            final PullMessageId pullMessageId = messagingLockDao.getNextPullMessageToProcess(sqlRowSet.getLong(1));
            if (pullMessageId != null) {
                LOG.debug("Message retrieved [{}] \n", pullMessageId);
                final String messageId = pullMessageId.getMessageId();
                switch (pullMessageId.getState()) {
                    case STALED:
                        LOG.debug("Message with id:[{}] is staled for reason:[{}]", pullMessageId.getMessageId(), pullMessageId.getStaledReason());
                        pullMessageStateService.messageStaled(messageId);
                        break;
                    case FIRST_ATTEMPT:
                        return messageId;
                    case FURTHER_ATTEMPT:
                        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(messageId);
                        return messageId;
                }
            }
        }
        LOG.debug("Returning null message\n");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addPullMessageLock(final PartyIdExtractor partyIdExtractor, UserMessage userMessage, MessageLog messageLog) {
        String partyId = partyIdExtractor.getPartyId();
        final String messageId = messageLog.getMessageId();
        final String mpc = messageLog.getMpc();
        LOG.trace("Saving message lock with id:[{}],partyID:[{}], mpc:[{}]", messageId, partyId, mpc);
        final String pmodeKey; // FIXME: This does not work for signalmessages
        try {
            pmodeKey = this.pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
        } catch (EbMS3Exception e) {
            throw new PModeException(DomibusCoreErrorCode.DOM_001, "Could not get the PMode key for message [" + messageId + "]", e);
        }
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final Date staledDate = getStaledDate(messageLog, legConfiguration);

        MessagingLock messagingLock = new MessagingLock(
                messageId,
                partyId,
                mpc,
                messageLog.getReceived(),
                staledDate,
                messageLog.getNextAttempt(),
                messageLog.getSendAttempts(),
                messageLog.getSendAttemptsMax());
        messagingLockDao.save(messagingLock);
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
    public void resetWaitingForReceiptPullMessages() {
        final List<String> messagesToReset = userMessageLogDao.findPullWaitingForReceiptMessages();
        for (String messagedId : messagesToReset) {
            rawEnvelopeLogDao.deleteUserMessageRawEnvelope(messagedId);
            final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messagedId);
            if (userMessageLog.getSendAttempts() < userMessageLog.getSendAttemptsMax()) {
                //retrive the message locK.
                final MessagingLock messagingLock = messagingLockDao.findMessagingLockForMessageId(messagedId);
                //could happen due to different transactions. So in oder to be resilient we check it.
                if (messagingLock == null) {
                    addPullMessageLock(userMessageLog);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Message " + messagedId + " set back in READY_TO_PULL state.");
                }
                userMessageLog.setMessageStatus(MessageStatus.READY_TO_PULL);
                //notify ??
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Pull Message with " + messagedId + " marked as send failure after max retry attempt reached");
                }
                userMessageLog.setMessageStatus(MessageStatus.SEND_FAILURE);
                deletePullMessageLock(messagedId);
                //notify.
            }
            userMessageLogDao.update(userMessageLog);
        }
    }


    /**
     * When a message has been set in waiting_for_receipt state its locking record has been deleted. When the retry
     * service set timed_out waiting_for_receipt messages back in ready_to_pull state, the search and lock system has to be fed again
     * with the message information.
     *
     * @param userMessageLog the messageLod
     */
    private void addPullMessageLock(final UserMessageLog userMessageLog) {
        final String messageId = userMessageLog.getMessageId();
        Messaging messageByMessageId = messagingDao.findMessageByMessageId(messageId);
        final UserMessage userMessage = messageByMessageId.getUserMessage();
        To to = userMessage.getPartyInfo().getTo();
        addPullMessageLock(new ToExtractor(to), userMessage, userMessageLog);
    }
}
