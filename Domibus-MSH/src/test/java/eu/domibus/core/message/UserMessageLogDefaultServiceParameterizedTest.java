package eu.domibus.core.message;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.model.MessageSubtype;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@RunWith(Parameterized.class)
public class UserMessageLogDefaultServiceParameterizedTest {

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    BackendNotificationService backendNotificationService;

    @Tested
    UserMessageLogDefaultService userMessageLogDefaultService;

    @Parameterized.Parameter(0)
    public MessageSubtype messageSubtype;

    @Parameterized.Parameters(name = "{index}: messageSubtype=\"{0}\"")
    public static Collection<Object[]> values() {
        return Arrays.asList(new Object[][]{
                {null},
                {MessageSubtype.TEST}
        });
    }

    @Test
    public void testSave() {
        final String messageId = "1";
        final String messageStatus = MessageStatus.SEND_ENQUEUED.toString();
        final String notificationStatus = NotificationStatus.NOTIFIED.toString();
        final String mshRole = MSHRole.SENDING.toString();
        final Integer maxAttempts = 10;
        final String mpc = " default";
        final String backendName = "JMS";
        final String endpoint = "http://localhost";

        userMessageLogDefaultService.save(messageId, messageStatus, notificationStatus, mshRole, maxAttempts, mpc, backendName, endpoint, messageSubtype != null ? messageSubtype.toString() : null);

        new Verifications() {{
            backendNotificationService.notifyOfMessageStatusChange(withAny(new UserMessageLog()), MessageStatus.SEND_ENQUEUED, withAny(new Timestamp(System.currentTimeMillis())));

            UserMessageLog userMessageLog;
            userMessageLogDao.create(userMessageLog = withCapture());
            Assert.assertEquals(messageId, userMessageLog.getMessageId());
            Assert.assertEquals(MessageStatus.SEND_ENQUEUED, userMessageLog.getMessageStatus());
            Assert.assertEquals(NotificationStatus.NOTIFIED, userMessageLog.getNotificationStatus());
            Assert.assertEquals(MSHRole.SENDING, userMessageLog.getMshRole());
            Assert.assertEquals(maxAttempts.intValue(), userMessageLog.getSendAttemptsMax());
            Assert.assertEquals(mpc, userMessageLog.getMpc());
            Assert.assertEquals(backendName, userMessageLog.getBackend());
            Assert.assertEquals(endpoint, userMessageLog.getEndpoint());
            Assert.assertEquals(messageSubtype, userMessageLog.getMessageSubtype());
        }};
    }
}
