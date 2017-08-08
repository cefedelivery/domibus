package eu.domibus.core.message;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.pmode.domain.LegConfiguration;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.UserMessageServiceHelper;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.messaging.DispatchMessageCreator;
import eu.domibus.plugin.NotificationListener;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class UserMessageDefaultServiceTest {

    private static final long SYSTEM_DATE = new Date().getTime();

    @Tested
    UserMessageDefaultService userMessageDefaultService;

    @Injectable
    private Queue sendMessageQueue;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private UserMessageLogService userMessageLogService;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private UserMessageServiceHelper userMessageServiceHelper;

    @Injectable
    private SignalMessageDao signalMessageDao;

    @Injectable
    private SignalMessageLogDao signalMessageLogDao;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    PModeService pModeService;

    @Injectable
    PModeServiceHelper pModeServiceHelper;

    @Test
    public void testGetFinalRecipient(@Injectable  final UserMessage userMessage) throws Exception {
        final String messageId = "1";

        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

        }};

        userMessageDefaultService.getFinalRecipient(messageId);

        new Verifications() {{
            userMessageServiceHelper.getFinalRecipient(userMessage);
        }};
    }

    @Test
    public void testGetFinalRecipientWhenNoMessageIsFound(@Injectable  final UserMessage userMessage) throws Exception {
        final String messageId = "1";

        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = null;

        }};

        Assert.assertNull(userMessageDefaultService.getFinalRecipient(messageId));
    }

    @Test
    public void testFailedMessages(@Injectable  final UserMessage userMessage) throws Exception {
        final String finalRecipient = "C4";

        userMessageDefaultService.getFailedMessages(finalRecipient);

        new Verifications() {{
            userMessageLogDao.findFailedMessages(finalRecipient);
        }};
    }

    @Test
    public void testGetFailedMessageElapsedTime(@Injectable  final UserMessageLog userMessageLog) throws Exception {
        final String messageId = "1";
        final Date failedDate = new Date();

        new CurrentTimeMillisMock();

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            userMessageLog.getFailed();
            result = failedDate;
        }};

        final Long failedMessageElapsedTime = userMessageDefaultService.getFailedMessageElapsedTime(messageId);
        Assert.assertTrue(SYSTEM_DATE - failedDate.getTime() == failedMessageElapsedTime);
    }

    @Test(expected = UserMessageException.class)
    public void testGetFailedMessageElapsedTimeWhenFailedDateIsNull(@Injectable  final UserMessageLog userMessageLog) throws Exception {
        final String messageId = "1";

        new CurrentTimeMillisMock();

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            userMessageLog.getFailed();
            result = null;
        }};

        userMessageDefaultService.getFailedMessageElapsedTime(messageId);
    }

    @Test(expected = UserMessageException.class)
    public void testRestoreMessageWhenMessageIsDeleted(@Injectable  final UserMessageLog userMessageLog) throws Exception {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.DELETED;

        }};

        userMessageDefaultService.restoreFailedMessage(messageId);
    }

    @Test
    public void testRestoreMessage(@Injectable final UserMessageLog userMessageLog) throws Exception {
        final String messageId = "1";
        final Integer newMaxAttempts = 5;

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            userMessageDefaultService.computeNewMaxAttempts(userMessageLog, messageId);
            result = newMaxAttempts;

        }};

        userMessageDefaultService.restoreFailedMessage(messageId);

        new Verifications() {{
            userMessageLog.setMessageStatus(MessageStatus.SEND_ENQUEUED);
            userMessageLog.setRestored(withAny(new Date()));
            userMessageLog.setFailed(null);
            userMessageLog.setNextAttempt(withAny(new Date()));
            userMessageLog.setSendAttemptsMax(newMaxAttempts);

            userMessageLogDao.update(userMessageLog);
            userMessageDefaultService.scheduleSending(messageId);
        }};
    }

    @Test
    public void testMaxAttemptsConfigurationWhenNoLegIsFound() throws Exception {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            pModeService.getLegConfiguration(messageId);
            result = null;

        }};

        final Integer maxAttemptsConfiguration = userMessageDefaultService.getMaxAttemptsConfiguration(messageId);
        Assert.assertTrue(maxAttemptsConfiguration == 1);

    }

    @Test
    public void testMaxAttemptsConfiguration(@Injectable final LegConfiguration legConfiguration) throws Exception {
        final String messageId = "1";
        final Integer pModeMaxAttempts = 5;

        new Expectations(userMessageDefaultService) {{
            pModeService.getLegConfiguration(messageId);
            result = legConfiguration;

            pModeServiceHelper.getMaxAttempts(legConfiguration);
            result = pModeMaxAttempts;

        }};

        final Integer maxAttemptsConfiguration = userMessageDefaultService.getMaxAttemptsConfiguration(messageId);
        Assert.assertTrue(maxAttemptsConfiguration == pModeMaxAttempts);
    }

    @Test
    public void testComputeMaxAttempts(@Injectable final UserMessageLog userMessageLog) throws Exception {
        final String messageId = "1";
        final Integer messageMaxAttempts = 2;
        final Integer pModeMaxAttempts = 5;

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getMaxAttemptsConfiguration(messageId);
            result = pModeMaxAttempts;

            userMessageLog.getSendAttemptsMax();
            result = messageMaxAttempts;

        }};

        final Integer maxAttemptsConfiguration = userMessageDefaultService.computeNewMaxAttempts(userMessageLog, messageId);
        Assert.assertTrue(maxAttemptsConfiguration == 7);
    }

    @Test
    public void testScheduleSending(@Injectable final JmsMessage jmsMessage, final @Mocked DispatchMessageCreator dispatchMessageCreator) throws Exception {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            new DispatchMessageCreator(messageId);
            result = dispatchMessageCreator;

            dispatchMessageCreator.createMessage();
            result = jmsMessage;

        }};

        userMessageDefaultService.scheduleSending(messageId);

        new Verifications() {{
            jmsManager.sendMessageToQueue(jmsMessage, sendMessageQueue);
        }};

    }

    @Test
    public void testRestoreFailedMessagesDuringPeriodWhenAPreviousMessageIsFailing() throws Exception {
        final String finalRecipient = "C4";
        final Date startDate = new Date();
        final Date endDate = new Date();

        final String failedMessage1 = "1";
        final String failedMessage2 = "2";
        final List<String> failedMessages = new ArrayList<>();
        failedMessages.add(failedMessage1);
        failedMessages.add(failedMessage2);

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findFailedMessages(finalRecipient, startDate, endDate);
            result = failedMessages;

            userMessageDefaultService.restoreFailedMessage(failedMessage1);

            userMessageDefaultService.restoreFailedMessage(failedMessage2);
            result = new RuntimeException("Problem restoring message 2");
        }};

        final List<String> restoredMessages = userMessageDefaultService.restoreFailedMessagesDuringPeriod(startDate, endDate, finalRecipient);
        assertNotNull(restoredMessages);
        assertEquals(restoredMessages.size(), 1);
        assertEquals(restoredMessages.iterator().next(), failedMessage1);
    }

    @Test
    public void testRestoreFailedMessagesDuringPeriod() throws Exception {
        final String finalRecipient = "C4";
        final Date startDate = new Date();
        final Date endDate = new Date();

        final String failedMessage1 = "1";
        final String failedMessage2 = "2";
        final List<String> failedMessages = new ArrayList<>();
        failedMessages.add(failedMessage1);
        failedMessages.add(failedMessage2);

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findFailedMessages(finalRecipient, startDate, endDate);
            result = failedMessages;

            userMessageDefaultService.restoreFailedMessage(anyString);
        }};

        final List<String> restoredMessages = userMessageDefaultService.restoreFailedMessagesDuringPeriod(startDate, endDate, finalRecipient);
        assertNotNull(restoredMessages);
        assertEquals(restoredMessages, failedMessages);
    }

    @Test(expected = UserMessageException.class)
    public void testFailedMessageWhenNoMessageIsFound(@Injectable final UserMessageLog userMessageLog) throws Exception {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = null;
        }};

        userMessageDefaultService.getFailedMessage(messageId);
    }

    @Test(expected = UserMessageException.class)
    public void testFailedMessageWhenStatusIsNotFailed(@Injectable final UserMessageLog userMessageLog) throws Exception {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.RECEIVED;
        }};

        userMessageDefaultService.getFailedMessage(messageId);
    }

    @Test
    public void testGetFailedMessage(@Injectable final UserMessageLog userMessageLog) throws Exception {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_FAILURE;
        }};

        final UserMessageLog failedMessage = userMessageDefaultService.getFailedMessage(messageId);
        Assert.assertNotNull(failedMessage);
    }

    @Test
    public void testDeleteMessageWhenNoNotificationListenerIsFound(@Injectable final UserMessageLog userMessageLog) throws Exception {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.handleSignalMessageDelete(messageId);
        }};

        userMessageDefaultService.deleteMessage(messageId);

        new Verifications() {{
            messagingDao.clearPayloadData(messageId);
            userMessageLogService.setMessageAsDeleted(messageId);
        }};
    }

    @Test
    public void testDeleteMessaged(@Mocked final NotificationListener notificationListener1) throws Exception {
        final String messageId = "1";
        final String queueName = "wsQueue";

        final List<NotificationListener> notificationListeners = new ArrayList<>();
        notificationListeners.add(notificationListener1);

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.handleSignalMessageDelete(messageId);

            backendNotificationService.getNotificationListenerServices();
            result = notificationListeners;

            notificationListener1.getBackendNotificationQueue().getQueueName();
            result = queueName;
        }};

        userMessageDefaultService.deleteMessage(messageId);

        new Verifications() {{
            messagingDao.clearPayloadData(messageId);
            userMessageLogService.setMessageAsDeleted(messageId);

            jmsManager.consumeMessage(queueName, messageId);
        }};
    }

    private static class CurrentTimeMillisMock extends MockUp<System> {
        @Mock
        public static long currentTimeMillis() {
            return SYSTEM_DATE;
        }
    }
}
