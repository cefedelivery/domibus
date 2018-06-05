package eu.domibus.core.pull;

import com.google.common.collect.Lists;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.common.ErrorCode;
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
import eu.domibus.ebms3.sender.UpdateRetryLoggingService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.util.Date;

import static eu.domibus.core.pull.PullMessageServiceImpl.PULL_EXTRA_NUMBER_OF_ATTEMPT_TIME_FOR_EXPIRATION_DATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(JMockit.class)
public class PullMessageServiceImplTest {

    @Injectable
    private UserMessageLogService userMessageLogService;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private PullMessageStateService pullMessageStateService;

    @Injectable
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Injectable
    private MessagingLockDao messagingLockDao;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private java.util.Properties domibusProperties;

    @Injectable
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Tested
    private PullMessageServiceImpl pullMessageService;


    @Test
    public void updatePullMessageAfterRequest() {
    }

    @Test
    public void hasAttemptsLeft() {
    }

    @Test
    public void updatePullMessageAfterReceipt() {
    }

    @Test
    public void addPullMessageLock() {
    }

    @Test
    public void deletePullMessageLock() {
    }

    @Test
    public void getPullMessageIdFirstAttempt(@Mocked final MessagingLock messagingLock, @Mocked final PullMessageId pullMessageId) {
        final String initiator = "initiator";
        final String mpc = "mpc";
        final String messageId = "messageId";
        final int id = 99;
        new Expectations() {{

            messagingLockDao.findReadyToPull(mpc, initiator);
            result = Lists.newArrayList(messagingLock);

            messagingLock.getMessageId();
            result = messageId;

            messagingLock.getEntityId();
            this.result = id;

            messagingLockDao.getNextPullMessageToProcess(id);
            result = pullMessageId;

            pullMessageId.getState();
            result = PullMessageState.FIRST_ATTEMPT;

            pullMessageId.getMessageId();
            result = messageId;

        }};
        final String returnedMessageId = pullMessageService.getPullMessageId(initiator, mpc);
        assertEquals(messageId, returnedMessageId);

        new Verifications() {{
            pullMessageStateService.expirePullMessage(messageId);
            times = 0;
        }};

    }

    @Test
    public void getPullMessageIdExpired(@Mocked final MessagingLock messagingLock, @Mocked final PullMessageId pullMessageId) {
        final String initiator = "initiator";
        final String mpc = "mpc";
        final String messageId = "messageId";
        final int id = 99;
        new Expectations() {{

            messagingLockDao.findReadyToPull(mpc, initiator);
            result = Lists.newArrayList(messagingLock);

            messagingLock.getMessageId();
            result = messageId;

            messagingLock.getEntityId();
            this.result = id;

            messagingLockDao.getNextPullMessageToProcess(id);
            result = pullMessageId;

            pullMessageId.getState();
            result = PullMessageState.EXPIRED;

            pullMessageId.getMessageId();
            result = messageId;

        }};
        final String returnedMessageId = pullMessageService.getPullMessageId(initiator, mpc);
        assertNull(returnedMessageId);

        new Verifications() {{
            pullMessageStateService.expirePullMessage(messageId);
            times = 1;
        }};

    }

    @Test
    public void getPullMessageIdRetry(@Mocked final MessagingLock messagingLock, @Mocked final PullMessageId pullMessageId) {
        final String initiator = "initiator";
        final String mpc = "mpc";
        final String messageId = "messageId";
        final int id = 99;
        new Expectations() {{

            messagingLockDao.findReadyToPull(mpc, initiator);
            result = Lists.newArrayList(messagingLock);

            messagingLock.getMessageId();
            result = messageId;

            messagingLock.getEntityId();
            this.result = id;

            messagingLockDao.getNextPullMessageToProcess(id);
            result = pullMessageId;

            pullMessageId.getState();
            result = PullMessageState.RETRY;

            pullMessageId.getMessageId();
            result = messageId;

        }};
        final String returnedMessageId = pullMessageService.getPullMessageId(initiator, mpc);
        assertEquals(messageId,returnedMessageId);

        new Verifications() {{
            rawEnvelopeLogDao.deleteUserMessageRawEnvelope(messageId);
            times = 1;
        }};

    }

    @Test(expected = PModeException.class)
    public void addPullMessageLockWithPmodeException(@Mocked final PartyIdExtractor partyIdExtractor, @Mocked final UserMessage userMessage, @Mocked final MessageLog messageLog) throws EbMS3Exception {
        final String pmodeKey = "pmodeKey";
        final String partyId = "partyId";
        final String messageId = "messageId";
        final String mpc = "mpc";
        final Date staledDate = new Date();
        final LegConfiguration legConfiguration = new LegConfiguration();
        new Expectations(pullMessageService) {{
            partyIdExtractor.getPartyId();
            result = partyId;
            messageLog.getMessageId();
            result = messageId;
            messageLog.getMpc();
            result = mpc;
            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "", "", null);
        }};

        pullMessageService.addPullMessageLock(partyIdExtractor, userMessage, messageLog);
    }

    @Test
    public void addPullMessageLock(@Mocked final PartyIdExtractor partyIdExtractor, @Mocked final UserMessage userMessage, @Mocked final MessageLog messageLog) throws EbMS3Exception {
        final String pmodeKey = "pmodeKey";
        final String partyId = "partyId";
        final String messageId = "messageId";
        final String mpc = "mpc";
        final Date staledDate = new Date();
        final LegConfiguration legConfiguration = new LegConfiguration();
        new Expectations(pullMessageService) {{
            partyIdExtractor.getPartyId();
            result = partyId;
            messageLog.getMessageId();
            result = messageId;
            messageLog.getMpc();
            result = mpc;
            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pmodeKey;
            pModeProvider.getLegConfiguration(pmodeKey);
            result = legConfiguration;
            pullMessageService.getPullMessageExpirationDate(messageLog, legConfiguration);
            result = staledDate;
        }};
        pullMessageService.addPullMessageLock(partyIdExtractor, userMessage, messageLog);
        new Verifications() {{
            MessagingLock messagingLock = null;
            messagingLockDao.save(messagingLock = withCapture());
            assertEquals(partyId, messagingLock.getInitiator());
            assertEquals(mpc, messagingLock.getMpc());
            assertEquals(messageId, messagingLock.getMessageId());
            assertEquals(staledDate, messagingLock.getStaled());

        }};
    }


    @Test
    public void getPullMessageExpirationDate(@Mocked final MessageLog userMessageLog, @Mocked final LegConfiguration legConfiguration) {

        final int extraAttemptsToAddToExpirationDate = 2;
        new MockUp<PullMessageServiceImpl>() {
            @Mock
            public int getExtraNumberOfAttemptTimeForExpirationDate() {
                return extraAttemptsToAddToExpirationDate;
            }
        };

        final long currentTime = System.currentTimeMillis();
        final int timeOut = 10;
        final long timeOutInMillis = 60000 * timeOut;
        final int retryCount = 3;
        final Date expectedDate = new Date(currentTime + timeOutInMillis + (extraAttemptsToAddToExpirationDate * (timeOutInMillis / retryCount)));
        new Expectations() {{
            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            result = currentTime;
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = timeOut;
            legConfiguration.getReceptionAwareness().getRetryCount();
            result = retryCount;
        }};
        assertEquals(expectedDate, pullMessageService.getPullMessageExpirationDate(userMessageLog, legConfiguration));

    }

    @Test
    public void waitingForCallExpired(
            @Mocked final MessagingLock lock,
            @Mocked final LegConfiguration legConfiguration,
            @Mocked final UserMessageLog userMessageLog,
            @Mocked final Timestamp timestamp) {
        new Expectations(pullMessageService){{
            messagingLockDao.findMessagingLockForMessageId(userMessageLog.getMessageId());
            result=lock;

            pullMessageService.isExpired(legConfiguration, userMessageLog);
            result=true;

        }};
        pullMessageService.waitingForCallBack(legConfiguration, userMessageLog);
        new Verifications() {{
            pullMessageStateService.sendFailed(userMessageLog);
            lock.setNextAttempt(null);
            lock.setMessageState(MessageState.DEL);
            messagingLockDao.save(lock);
            userMessageLogDao.update(userMessageLog);times=0;
        }};
    }

    @Test
    public void waitingForCallBackWithAttempt(
            @Mocked final MessagingLock lock,
            @Mocked final LegConfiguration legConfiguration,
            @Mocked final UserMessageLog userMessageLog,
            @Mocked final Timestamp timestamp) {
        new Expectations(pullMessageService){{
            messagingLockDao.findMessagingLockForMessageId(userMessageLog.getMessageId());
            result=lock;

            pullMessageService.isExpired(legConfiguration, userMessageLog);
            result=false;

            pullMessageService.updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
        }};
        pullMessageService.waitingForCallBack(legConfiguration, userMessageLog);
        new Verifications() {{
            lock.setMessageState(MessageState.WAITING);
            lock.setSendAttempts(userMessageLog.getSendAttempts());
            lock.setNextAttempt(userMessageLog.getNextAttempt());
            userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RECEIPT);
            messagingLockDao.save(lock);
            userMessageLogDao.update(userMessageLog);
            backendNotificationService.notifyOfMessageStatusChange(userMessageLog, MessageStatus.WAITING_FOR_RECEIPT, withAny(timestamp));
        }};
    }

    @Test
    public void hasAttemptsLeftTrueBecauseOfSendAttempt(@Mocked final MessageLog userMessageLog, @Mocked final LegConfiguration legConfiguration) {
        new Expectations() {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = 1;
            legConfiguration.getReceptionAwareness().getRetryCount();
            result = 10;
            userMessageLog.getSendAttempts();
            result = 1;
            userMessageLog.getSendAttemptsMax();
            result = 2;
            domibusProperties.getProperty(PULL_EXTRA_NUMBER_OF_ATTEMPT_TIME_FOR_EXPIRATION_DATE, "2");
            result = "2";
            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            result = System.currentTimeMillis() - 50000;
        }};
        assertEquals(true, pullMessageService.attemptNumberLeftIsLowerOrEqualThenMaxAttempts(userMessageLog, legConfiguration));
    }

    @Test
    public void hasAttemptsLeftFalseBecauseOfSendAttempt(@Mocked final MessageLog userMessageLog, @Mocked final LegConfiguration legConfiguration) {
        new Expectations() {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            times = 0;
            userMessageLog.getSendAttempts();
            result = 3;
            userMessageLog.getSendAttemptsMax();
            result = 2;
            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            times = 0;
        }};
        assertEquals(false, pullMessageService.attemptNumberLeftIsLowerOrEqualThenMaxAttempts(userMessageLog, legConfiguration));
    }

    @Test
    public void hasAttemptsLeftFalseBecauseOfRetry(@Mocked final MessageLog userMessageLog, @Mocked final LegConfiguration legConfiguration) {
        new Expectations() {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = 1;

            legConfiguration.getReceptionAwareness().getRetryCount();
            result = 10;

            userMessageLog.getSendAttempts();
            result = 2;

            userMessageLog.getSendAttemptsMax();
            result = 2;

            domibusProperties.getProperty(PULL_EXTRA_NUMBER_OF_ATTEMPT_TIME_FOR_EXPIRATION_DATE, "2");
            result="2";

            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            result = System.currentTimeMillis() - 70000;
        }};
        assertEquals(true, pullMessageService.attemptNumberLeftIsLowerOrEqualThenMaxAttempts(userMessageLog, legConfiguration));
    }

    @Test
    public void pullFailedOnRequestWithNoAttempt(@Mocked final MessagingLock lock,@Mocked final LegConfiguration legConfiguration, @Mocked final UserMessageLog userMessageLog) {

        final String messageID = "123456";
        new Expectations(pullMessageService) {{
            userMessageLog.getMessageId();
            result = messageID;

            messagingLockDao.findMessagingLockForMessageId(userMessageLog.getMessageId());
            result=lock;

            pullMessageService.attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration);
            result=false;
        }};

        pullMessageService.pullFailedOnRequest(legConfiguration, userMessageLog);
        new VerificationsInOrder() {{
            lock.setNextAttempt(null);
            lock.setMessageState(MessageState.DEL);
            pullMessageStateService.sendFailed(userMessageLog);
            messagingLockDao.save(lock);
        }};
    }

    @Test
    public void pullFailedOnRequestWithAttempt(@Mocked final MessagingLock lock, @Mocked final LegConfiguration legConfiguration, @Mocked final UserMessageLog userMessageLog) {

        final String messageID = "123456";
        final Date nextAttempt = new Date(1528110891749l);
        new Expectations(pullMessageService) {{
            userMessageLog.getMessageId();
            result = messageID;

            messagingLockDao.findMessagingLockForMessageId(userMessageLog.getMessageId());
            result = lock;

            pullMessageService.attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration);
            result = true;

            userMessageLog.getSendAttempts();
            result = 3;

            userMessageLog.getNextAttempt();
            result = nextAttempt;
        }};

        pullMessageService.pullFailedOnRequest(legConfiguration, userMessageLog);
        new VerificationsInOrder() {{
            updateRetryLoggingService.saveAndNotify(MessageStatus.READY_TO_PULL, userMessageLog);
            lock.setMessageState(MessageState.READY);
            lock.setSendAttempts(3);
            lock.setNextAttempt(nextAttempt);
            messagingLockDao.save(lock);
        }};
    }

    @Test
    public void pullFailedOnReceiptWithAttemptLeft(@Mocked final LegConfiguration legConfiguration, @Mocked final UserMessageLog userMessageLog) {
        final String messageID = "123456";
        new Expectations(pullMessageService) {{
            userMessageLog.getMessageId();
            result = messageID;
            pullMessageService.attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration);
            result = true;
        }};
        pullMessageService.pullFailedOnReceipt(legConfiguration, userMessageLog);
        new VerificationsInOrder() {{
            backendNotificationService.notifyOfMessageStatusChange(userMessageLog, MessageStatus.READY_TO_PULL, withAny(new Timestamp(0)));
            times = 1;
            pullMessageStateService.reset(userMessageLog);
            times = 1;
        }};

    }

    @Test
    public void pullFailedOnReceiptWithNoAttemptLeft(@Mocked final LegConfiguration legConfiguration, @Mocked final UserMessageLog userMessageLog) {
        final String messageID = "123456";
        new Expectations(pullMessageService) {{
            userMessageLog.getMessageId();
            result = messageID;

            pullMessageService.attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration);
            result = false;
        }};
        pullMessageService.pullFailedOnReceipt(legConfiguration, userMessageLog);

        new VerificationsInOrder() {{
            pullMessageStateService.sendFailed(userMessageLog);
        }};

    }
}