package eu.domibus.core.message;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.Ebms3Constants;
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

    @Tested
    private UserMessageLogDefaultService userMessageLogDefaultService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Parameterized.Parameter(0)
    public String service;

    @Parameterized.Parameter(1)
    public String action;

    @Parameterized.Parameters(name = "{index}: service=\"{0}\" action=\"{1}\"")
    public static Collection<Object[]> values() {
        return Arrays.asList(new Object[][]{
                {"service","action"},
                {Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION}
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

        userMessageLogDefaultService.save(messageId, messageStatus, notificationStatus, mshRole, maxAttempts, mpc, backendName, endpoint, service, action);

        new Verifications() {{
            backendNotificationService.notifyOfMessageStatusChange(withAny(new UserMessageLog()), MessageStatus.SEND_ENQUEUED, withAny(new Timestamp(System.currentTimeMillis())));
            times = userMessageLogDefaultService.checkTestMessage(service,action)?0:1;

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
            Assert.assertEquals(userMessageLogDefaultService.checkTestMessage(service,action)? MessageSubtype.TEST : null, userMessageLog.getMessageSubtype());
        }};
    }
}
