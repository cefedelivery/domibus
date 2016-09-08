package eu.domibus.messaging;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.NotificationType;
import eu.domibus.jms.spi.JmsMessageSPI;
import eu.domibus.messaging.jms.JMSMessageMapper;
import mockit.Expectations;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Cosmin Baciu on 02-Sep-16.
 */
@RunWith(JMockit.class)
public class NotifyMessageCreatorTest {

    @Test
    public void testCreateMessage() throws Exception {
        NotifyMessageCreator creator = new NotifyMessageCreator("myMessageId", NotificationType.MESSAGE_RECEIVED);
        JmsMessage message = creator.createMessage();
        assertEquals(message.getProperty(MessageConstants.MESSAGE_ID), "myMessageId");
        assertEquals(message.getProperty(MessageConstants.NOTIFICATION_TYPE), NotificationType.MESSAGE_RECEIVED.name());
    }
}
