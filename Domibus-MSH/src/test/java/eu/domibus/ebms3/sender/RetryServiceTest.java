package eu.domibus.ebms3.sender;

import com.google.common.collect.Lists;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.pull.MessagingLockDao;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.core.pull.PullMessageStateService;
import eu.domibus.ebms3.common.model.MessagingLock;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(JMockit.class)
public class RetryServiceTest {

    @Injectable
    private PullMessageService pullMessageService;

    @Injectable
    private PullMessageStateService pullMessageStateService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private MessagingLockDao messagingLockDao;

    @Tested
    private RetryService retryService;

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
        retryService.resetWaitingForReceiptPullMessages();
        new VerificationsInOrder() {{
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
        retryService.resetWaitingForReceiptPullMessages();
        new VerificationsInOrder() {{
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
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;
            userMessageLog.getSendAttempts();
            result = 1;
            userMessageLog.getSendAttemptsMax();
            result = 2;
            messagingLockDao.findMessagingLockForMessageId(messageId);
            result = null;
        }};
        retryService.resetWaitingForReceiptPullMessages();
        new VerificationsInOrder() {{
            times = 1;
            pullMessageStateService.reset(userMessageLog);
        }};
    }

}