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
    }

}
