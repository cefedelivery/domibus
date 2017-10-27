package eu.domibus.messaging;

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Created by Cosmin Baciu on 02-Sep-16.
 */
@RunWith(JMockit.class)
public class JMSMessageBuilderTest {

    @Test
    public void testCreateMessage() throws Exception {
        JmsMessage message = JMSMessageBuilder
                .create()
                .property("stringProp", "myString")
                .property("integerProp", 100)
                .property("longProp", 200L)
                .build();
        assertEquals(message.getProperty("stringProp"), "myString");
        assertEquals(message.getProperty("integerProp"), Integer.valueOf(100));
        assertEquals(message.getProperty("longProp"), Long.valueOf(200L));
    }
}
