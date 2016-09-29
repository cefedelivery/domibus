package eu.domibus.common.model.logging;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.ebms3.common.model.MessageType;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author Federico Martini
 * @since 3.2
 */
@RunWith(JMockit.class)
public class MessageLogBuilderTest {

    @Test
    public void testSignalMessageLogResolver() throws Exception {

        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";

        // Builds the signal message log
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                .setMessageId(messageId)
                .setMessageStatus(MessageStatus.ACKNOWLEDGED)
                .setMshRole(MSHRole.RECEIVING)
                .setNotificationStatus(NotificationStatus.NOT_REQUIRED);

        SignalMessageLog signalMessageLog = smlBuilder.build();

        assertEquals(signalMessageLog.getMessageType(), MessageType.SIGNAL_MESSAGE);
        assertEquals(signalMessageLog.getMessageId(), messageId);
        assertEquals(signalMessageLog.getMessageStatus(), MessageStatus.ACKNOWLEDGED);
        assertEquals(signalMessageLog.getMshRole(), MSHRole.RECEIVING);
        assertEquals(signalMessageLog.getNotificationStatus(), NotificationStatus.NOT_REQUIRED);
        assertEquals(signalMessageLog.getReceived(), signalMessageLog.getNextAttempt());
        assertEquals(signalMessageLog.getSendAttempts(), 0);

    }

    @Test
    public void testUserMessageLogResolver() throws Exception {

        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";
        String defaultMpcQualifiedName = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC";
        String endpoint = "http://localhost:8180/domibus/services/msh";
        String backendName = "backendWebservice";

        // Builds the user message log
        UserMessageLogBuilder umlBuilder = UserMessageLogBuilder.create()
                .setMessageId(messageId)
                .setMessageStatus(MessageStatus.SEND_ENQUEUED)
                .setMshRole(MSHRole.SENDING)
                .setNotificationStatus(NotificationStatus.REQUIRED)
                .setMessageStatus(MessageStatus.SEND_ENQUEUED)
                .setMshRole(MSHRole.SENDING)
                .setMpc(defaultMpcQualifiedName)
                .setSendAttemptsMax(5)
                .setBackendName(backendName)
                .setEndpoint(endpoint);

        UserMessageLog userMessageLog = umlBuilder.build();

        assertEquals(userMessageLog.getMessageType(), MessageType.USER_MESSAGE);
        assertEquals(userMessageLog.getMessageId(), messageId);
        assertEquals(userMessageLog.getMessageStatus(), MessageStatus.SEND_ENQUEUED);
        assertEquals(userMessageLog.getMshRole(), MSHRole.SENDING);
        assertEquals(userMessageLog.getNotificationStatus(), NotificationStatus.REQUIRED);
        assertEquals(userMessageLog.getMpc(), defaultMpcQualifiedName);
        assertEquals(userMessageLog.getSendAttempts(), 0);
        assertEquals(userMessageLog.getSendAttemptsMax(), 5);
        assertEquals(userMessageLog.getBackend(), backendName);
        assertEquals(userMessageLog.getEndpoint(), endpoint);
        assertEquals(userMessageLog.getReceived(), userMessageLog.getNextAttempt());

    }

}
