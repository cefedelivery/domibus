package eu.domibus.common.model.logging;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.ebms3.common.model.MessageType;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

        assertEquals(MessageType.SIGNAL_MESSAGE, signalMessageLog.getMessageType());
        assertEquals(messageId, signalMessageLog.getMessageId());
        assertEquals(MessageStatus.ACKNOWLEDGED, signalMessageLog.getMessageStatus());
        assertEquals(MSHRole.RECEIVING, signalMessageLog.getMshRole());
        assertEquals(NotificationStatus.NOT_REQUIRED, signalMessageLog.getNotificationStatus());
        assertNull(signalMessageLog.getNextAttempt());
        assertEquals(0, signalMessageLog.getSendAttempts());

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

        assertEquals(MessageType.USER_MESSAGE, userMessageLog.getMessageType());
        assertEquals(messageId, userMessageLog.getMessageId());
        assertEquals(MessageStatus.SEND_ENQUEUED, userMessageLog.getMessageStatus());
        assertEquals(MSHRole.SENDING, userMessageLog.getMshRole());
        assertEquals(NotificationStatus.REQUIRED, userMessageLog.getNotificationStatus());
        assertEquals(defaultMpcQualifiedName, userMessageLog.getMpc());
        assertEquals(0, userMessageLog.getSendAttempts());
        assertEquals(5, userMessageLog.getSendAttemptsMax());
        assertEquals(backendName, userMessageLog.getBackend());
        assertEquals(endpoint, userMessageLog.getEndpoint());
        assertNull(userMessageLog.getNextAttempt());

    }

}
