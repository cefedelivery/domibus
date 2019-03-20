
package eu.domibus.ebms3.sender;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.UserMessageLogEntity;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.pull.MessagingLockDao;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JMockit.class)
public class RetryServiceTest {

    private static List<String> QUEUED_MESSAGEIDS = Arrays.asList("queued123@domibus.eu", "queued456@domibus.eu", "queued789@domibus.eu");
    private static List<String> RETRY_MESSAGEIDS = Arrays.asList("retry123@domibus.eu", "retry456@domibus.eu", "queued456@domibus.eu", "expired123@domibus.eu");

    @Tested
    private RetryService retryService;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private Queue sendMessageQueue;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private UserMessageLogService userMessageLogService;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private PullMessageService pullMessageService;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Injectable
    private MessagingLockDao messagingLockDao;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    UpdateRetryLoggingService updateRetryLoggingService;

    private List<JmsMessage> getQueuedMessages() {
        List<JmsMessage> jmsMessages = new ArrayList<>();
        for(String messageId : QUEUED_MESSAGEIDS) {
            JmsMessage jmsMessage = new JmsMessage();
            jmsMessage.setProperty(MessageConstants.MESSAGE_ID, messageId);
            jmsMessages.add(jmsMessage);
        }
        return jmsMessages;
    }

    @Test
    public void getQueuedMessagesTest() {
        new NonStrictExpectations() {{
            jmsManager.browseClusterMessages(anyString);
            result = getQueuedMessages();
        }};

        List<String> result = retryService.getQueuedMessages();
        assertEquals(3, result.size());
    }

    @Test
    public void getMessagesNotAlreadyQueuedTest() {
        List<String> expectedMessageIds = Arrays.asList("retry123@domibus.eu", "retry456@domibus.eu", "expired123@domibus.eu");
        new NonStrictExpectations() {{
            userMessageLogDao.findRetryMessages();
            result = new ArrayList<>(RETRY_MESSAGEIDS);
            jmsManager.browseClusterMessages(anyString);
            result = getQueuedMessages();
        }};

        List<String> result = retryService.getMessagesNotAlreadyQueued();

        assertFalse(result.contains("queued456@domibus.eu"));
        assertEquals(expectedMessageIds, result);
    }

    @Test
    public void failIfExpiredTest(@Injectable UserMessage userMessage) throws EbMS3Exception {
        new NonStrictExpectations() {{
            userMessageLogDao.findRetryMessages();
            result = new ArrayList<>(RETRY_MESSAGEIDS);
            jmsManager.browseClusterMessages(anyString);
            result = getQueuedMessages();
        }};

        List<String> messagesNotAlreadyQueued = retryService.getMessagesNotAlreadyQueued();

        final UserMessageLogEntity userMessageLog = new UserMessageLogEntity();
        userMessageLog.setSendAttempts(2);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);
        new NonStrictExpectations() {{
            userMessageLogDao.findByMessageId("expired123@domibus.eu", MSHRole.SENDING);
            result = userMessageLog;
            updateRetryLoggingService.isExpired((LegConfiguration) any, userMessageLog);
            result = true;
            updateRetryLoggingService.isExpired((LegConfiguration) any, (UserMessageLogEntity) any );
            result = false;
        }};
        assertTrue(retryService.failIfExpired("expired123@domibus.eu"));
        assertFalse(retryService.failIfExpired("retry123@domibus.eu"));

        for(String messageId : messagesNotAlreadyQueued) {
            retryService.failIfExpired(messageId);
        }
        new Verifications() {{
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog); times = 2; // one outside for and one in for
        }};
    }
}