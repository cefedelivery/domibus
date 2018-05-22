package eu.domibus.core.pull;

import com.google.common.collect.Lists;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.sender.UpdateRetryLoggingService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

    @Tested
    private PullMessageServiceImpl pullMessageService;

    @Test
    public void setDataSource(@Mocked NamedParameterJdbcTemplate namedParameterJdbcTemplate, @Mocked final DataSource dataSource) {
        pullMessageService.setDataSource(dataSource);
        new Verifications() {{
            final List<NamedParameterJdbcTemplate> namedParameterJdbcTemplates = withCapture(new NamedParameterJdbcTemplate(dataSource));
            assertEquals(1, namedParameterJdbcTemplates.size());
        }};
    }

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
    public void getPullMessageId() {
    }

    @Test
    public void addPullMessageLock() {
    }

    @Test
    public void deletePullMessageLock() {
    }

    @Test
    public void resetWaitingForReceiptPullMessagesWithNoAttempts(@Mocked final MessagingLock messagingLock) {
        final String messageId = "123456";
        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(2);
        userMessageLog.setSendAttemptsMax(2);
        final List<String> messages = Lists.newArrayList(messageId);
        new Expectations(pullMessageService) {{
            userMessageLogDao.findPullWaitingForReceiptMessages();
            result = messages;
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;
        }};
        pullMessageService.resetWaitingForReceiptPullMessages();
        new VerificationsInOrder() {{
            pullMessageService.addPullMessageLock(userMessageLog);
            times = 0;
            UserMessageLog userMessageLog1 = null;
            pullMessageStateService.sendFailed(userMessageLog);
        }};
    }

    @Test
    public void resetWaitingForReceiptPullMessagesWithNoLockAndAttempts(@Mocked final UserMessageLog userMessageLog, @Mocked final MessagingLock messagingLock) {
        final String messageId = "123456";
        final List<String> messages = Lists.newArrayList(messageId);
        new Expectations(pullMessageService) {{
            userMessageLogDao.findPullWaitingForReceiptMessages();
            result = messages;
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;
            userMessageLog.getSendAttempts();
            result = 1;
            userMessageLog.getSendAttemptsMax();
            result = 2;
            messagingLockDao.findMessagingLockForMessageId(messageId);
            result = messagingLock;
        }};
        pullMessageService.resetWaitingForReceiptPullMessages();
        new VerificationsInOrder() {{
            pullMessageService.addPullMessageLock(userMessageLog);
            times = 0;
            pullMessageStateService.reset(userMessageLog);
        }};
    }

    @Test
    public void resetWaitingForReceiptPullMessagesWithExistingLockAndAttempts(@Mocked final UserMessageLog userMessageLog) {
        final String messageId = "123456";
        final List<String> messages = Lists.newArrayList(messageId);
        new Expectations(pullMessageService) {{
            userMessageLogDao.findPullWaitingForReceiptMessages();
            result = messages;
            pullMessageService.addPullMessageLock(userMessageLog);
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;
            userMessageLog.getSendAttempts();
            result = 1;
            userMessageLog.getSendAttemptsMax();
            result = 2;
            messagingLockDao.findMessagingLockForMessageId(messageId);
            result = null;
        }};
        pullMessageService.resetWaitingForReceiptPullMessages();
        new VerificationsInOrder() {{
            pullMessageService.addPullMessageLock(userMessageLog);
            times = 1;
            pullMessageStateService.reset(userMessageLog);
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
    public void getExtraNumberOfAttemptTimeForExpirationDate() {
        new Expectations() {{
            domibusProperties.getProperty("pull.extra.number.of.attempt.time.for.expiration.date", "2");
            result = "2";
        }};
        assertEquals(2, pullMessageService.getExtraNumberOfAttemptTimeForExpirationDate());
    }

    @Test
    public void waitingForCallBackWithoutAttempt(@Mocked final ToExtractor toExtractor, @Mocked final UserMessage userMessage, @Mocked final LegConfiguration legConfiguration, @Mocked final UserMessageLog userMessageLog) {
        final String messageID = "123456";
        new MockUp<PullMessageServiceImpl>() {
            @Mock
            public boolean hasAttemptsLeft(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
                return false;
            }

            @Mock
            public void addPullMessageLock(final PartyIdExtractor partyIdExtractor, UserMessage userMessage, MessageLog messageLog) {
            }
        };
        pullMessageService.waitingForCallBack(userMessage, legConfiguration, userMessageLog);
        new Verifications() {{
            updateRetryLoggingService.updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
            times = 0;
            userMessageLogDao.update(userMessageLog);
            userMessage.getPartyInfo().getTo();
            times = 1;
            backendNotificationService.notifyOfMessageStatusChange(userMessageLog, MessageStatus.WAITING_FOR_RECEIPT, withAny(new Timestamp(System.currentTimeMillis())));
            pullMessageService.addPullMessageLock(toExtractor, userMessage, userMessageLog);
            times = 1;
        }};
    }

    @Test
    public void waitingForCallBackWithAttempt(@Mocked final ToExtractor toExtractor, @Mocked final UserMessage userMessage, @Mocked final LegConfiguration legConfiguration, @Mocked final UserMessageLog userMessageLog) {
        final String messageID = "123456";
        new MockUp<PullMessageServiceImpl>() {
            @Mock
            public boolean hasAttemptsLeft(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
                return true;
            }

            @Mock
            public void addPullMessageLock(final PartyIdExtractor partyIdExtractor, UserMessage userMessage, MessageLog messageLog) {
            }
        };
        pullMessageService.waitingForCallBack(userMessage, legConfiguration, userMessageLog);
        new Verifications() {{
            updateRetryLoggingService.updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
            times = 1;
            userMessageLogDao.update(userMessageLog);
            userMessage.getPartyInfo().getTo();
            times = 1;
            backendNotificationService.notifyOfMessageStatusChange(userMessageLog, MessageStatus.WAITING_FOR_RECEIPT, withAny(new Timestamp(System.currentTimeMillis())));
            pullMessageService.addPullMessageLock(toExtractor, userMessage, userMessageLog);
        }};
    }

    @Test
    public void hasAttemptsLeftTrueBecauseOfSendAttempt(@Mocked final MessageLog userMessageLog, @Mocked final LegConfiguration legConfiguration) {
        new Expectations() {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = 1;
            userMessageLog.getSendAttempts();
            result = 1;
            userMessageLog.getSendAttemptsMax();
            result = 2;
            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            result = System.currentTimeMillis() - 50000;
        }};
        assertEquals(true, pullMessageService.hasAttemptsLeft(userMessageLog, legConfiguration));
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
        assertEquals(false, pullMessageService.hasAttemptsLeft(userMessageLog, legConfiguration));
    }

    @Test
    public void hasAttemptsLeftFalseBecauseOfRetry(@Mocked final MessageLog userMessageLog, @Mocked final LegConfiguration legConfiguration) {
        new Expectations() {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = 1;
            userMessageLog.getSendAttempts();
            result = 2;
            userMessageLog.getSendAttemptsMax();
            result = 2;
            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            result = System.currentTimeMillis() - 70000;
        }};
        assertEquals(false, pullMessageService.hasAttemptsLeft(userMessageLog, legConfiguration));
    }

    @Test
    public void pullFailedOnRequestWithNoAttempt(@Mocked final ToExtractor toExtractor, @Mocked final UserMessage userMessage, @Mocked final LegConfiguration legConfiguration, @Mocked final UserMessageLog userMessageLog) {

        final String messageID = "123456";
        new MockUp<PullMessageServiceImpl>() {
            @Mock
            public void addPullMessageLock(final PartyIdExtractor partyIdExtractor, UserMessage userMessage, MessageLog messageLog) {
            }
        };
        new Expectations() {{
            userMessageLog.getMessageId();
            result = messageID;
            updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration);
            result = false;
        }};

        pullMessageService.pullFailedOnRequest(userMessage, legConfiguration, userMessageLog);
        new VerificationsInOrder() {{
            pullMessageStateService.sendFailed(userMessageLog);
        }};
    }

    @Test
    public void pullFailedOnRequestWithAttempt(@Mocked final ToExtractor toExtractor, @Mocked final UserMessage userMessage, @Mocked final LegConfiguration legConfiguration, @Mocked final UserMessageLog userMessageLog) {

        final String messageID = "123456";
        new MockUp<PullMessageServiceImpl>() {
            @Mock
            public void addPullMessageLock(final PartyIdExtractor partyIdExtractor, UserMessage userMessage, MessageLog messageLog) {
            }
        };
        new Expectations() {{
            userMessageLog.getMessageId();
            result = messageID;
            updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration);
            result = true;
        }};

        pullMessageService.pullFailedOnRequest(userMessage, legConfiguration, userMessageLog);
        new VerificationsInOrder() {{
            updateRetryLoggingService.increaseAttempAndNotify(legConfiguration, MessageStatus.READY_TO_PULL, userMessageLog);
            times = 1;
            pullMessageService.addPullMessageLock(toExtractor, userMessage, userMessageLog);
            times = 1;
        }};
    }

    @Test
    public void pullFailedOnReceiptWithAttemptLeft(@Mocked final LegConfiguration legConfiguration, @Mocked final UserMessageLog userMessageLog) {
        final String messageID = "123456";
        new Expectations() {{
            userMessageLog.getMessageId();
            result = messageID;
            updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration);
            result = true;
        }};
        pullMessageService.pullFailedOnReceipt(legConfiguration, userMessageLog);
        new VerificationsInOrder() {{
            rawEnvelopeLogDao.deleteUserMessageRawEnvelope(messageID);
            times = 1;
            backendNotificationService.notifyOfMessageStatusChange(userMessageLog, MessageStatus.READY_TO_PULL, withAny(new Timestamp(0)));
            times = 1;
            pullMessageStateService.reset(userMessageLog);
            times = 1;
        }};

    }

    @Test
    public void pullFailedOnReceiptWithNoAttemptLeft(@Mocked final LegConfiguration legConfiguration, @Mocked final UserMessageLog userMessageLog) {
        final String messageID = "123456";
        new Expectations() {{
            userMessageLog.getMessageId();
            result = messageID;
            updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration);
            result = false;
        }};
        pullMessageService.pullFailedOnReceipt(legConfiguration, userMessageLog);
        new VerificationsInOrder() {{
            messagingLockDao.delete(messageID);
            times = 1;
            pullMessageStateService.sendFailed(userMessageLog);
            times = 1;
        }};

    }
}