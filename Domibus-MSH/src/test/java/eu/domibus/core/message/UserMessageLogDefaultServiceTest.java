package eu.domibus.core.message;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class UserMessageLogDefaultServiceTest {

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    BackendNotificationService backendNotificationService;

    @Tested
    UserMessageLogDefaultService userMessageLogDefaultService;

    @Test
    public void testSave() throws Exception {
        final String messageId = "1";
        final String messageStatus = MessageStatus.SEND_ENQUEUED.toString();
        final String notificationStatus = NotificationStatus.NOTIFIED.toString();
        final String mshRole = MSHRole.SENDING.toString();
        final Integer maxAttempts = 10;
        final String mpc = " default";
        final String backendName = "JMS";
        final String endpoint = "http://localhost";

        userMessageLogDefaultService.save(messageId, messageStatus, notificationStatus, mshRole, maxAttempts, mpc, backendName, endpoint);

        new Verifications() {{
            backendNotificationService.notifyOfMessageStatusChange(withAny(new UserMessageLog()), MessageStatus.SEND_ENQUEUED, withAny(new Timestamp(System.currentTimeMillis())));

            UserMessageLog userMessageLog = null;
            userMessageLogDao.create(userMessageLog = withCapture());
            Assert.assertEquals(messageId, userMessageLog.getMessageId());
            Assert.assertEquals(MessageStatus.SEND_ENQUEUED, userMessageLog.getMessageStatus());
            Assert.assertEquals(NotificationStatus.NOTIFIED, userMessageLog.getNotificationStatus());
            Assert.assertEquals(MSHRole.SENDING, userMessageLog.getMshRole());
            Assert.assertEquals(maxAttempts.intValue(), userMessageLog.getSendAttemptsMax());
            Assert.assertEquals(mpc, userMessageLog.getMpc());
            Assert.assertEquals(backendName, userMessageLog.getBackend());
            Assert.assertEquals(endpoint, userMessageLog.getEndpoint());
        }};
    }

    @Test
    public void testUpdateMessageStatus(@Injectable final UserMessageLog messageLog) throws Exception {
        final String messageId = "1";
        final MessageStatus messageStatus = MessageStatus.SEND_ENQUEUED;

        new Expectations() {{
            userMessageLogDao.findByMessageId(messageId);
            result = messageLog;

            messageLog.getMessageType();
            result = MessageType.USER_MESSAGE;
        }};

        userMessageLogDefaultService.updateMessageStatus(messageId, messageStatus);

        new Verifications() {{
            backendNotificationService.notifyOfMessageStatusChange(messageLog, messageStatus, withAny(new Timestamp(System.currentTimeMillis())));
            userMessageLogDao.setMessageStatus(messageLog, messageStatus);
        }};
    }

    @Test
    public void testSetMessageAsDeleted() throws Exception {
        final String messageId = "1";
        userMessageLogDefaultService.setMessageAsDeleted(messageId);

        new Verifications() {{
            userMessageLogDefaultService.updateMessageStatus(messageId, MessageStatus.DELETED);
        }};
    }

    @Test
    public void testSetMessageAsDownloaded() throws Exception {
        final String messageId = "1";
        userMessageLogDefaultService.setMessageAsDownloaded(messageId);

        new Verifications() {{
            userMessageLogDefaultService.updateMessageStatus(messageId, MessageStatus.DOWNLOADED);
        }};
    }

    @Test
    public void testSetMessageAsAcknowledged() throws Exception {
        final String messageId = "1";
        userMessageLogDefaultService.setMessageAsAcknowledged(messageId);

        new Verifications() {{
            userMessageLogDefaultService.updateMessageStatus(messageId, MessageStatus.ACKNOWLEDGED);
        }};
    }

    @Test
    public void testSetMessageAsAckWithWarnings() throws Exception {
        final String messageId = "1";
        userMessageLogDefaultService.setMessageAsAckWithWarnings(messageId);

        new Verifications() {{
            userMessageLogDefaultService.updateMessageStatus(messageId, MessageStatus.ACKNOWLEDGED_WITH_WARNING);
        }};
    }

    @Test
    public void testSetMessageAsWaitingForReceipt() throws Exception {
        final String messageId = "1";
        userMessageLogDefaultService.setMessageAsWaitingForReceipt(messageId);

        new Verifications() {{
            userMessageLogDefaultService.updateMessageStatus(messageId, MessageStatus.WAITING_FOR_RECEIPT);
        }};
    }

    @Test
    public void tesSetMessageAsSendFailure() throws Exception {
        final String messageId = "1";
        userMessageLogDefaultService.setMessageAsSendFailure(messageId);

        new Verifications() {{
            userMessageLogDefaultService.updateMessageStatus(messageId, MessageStatus.SEND_FAILURE);
        }};
    }

    @Test
    public void tesSetIntermediaryPullStatus() throws Exception {
        final String messageId = "1";
        userMessageLogDefaultService.setIntermediaryPullStatus(messageId);

        new Verifications() {{
            userMessageLogDefaultService.updateMessageStatus(messageId, MessageStatus.BEING_PULLED);
        }};
    }
}
