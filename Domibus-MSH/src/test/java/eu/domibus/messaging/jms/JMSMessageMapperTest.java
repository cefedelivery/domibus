package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.jms.spi.InternalJmsMessage;
import mockit.Expectations;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Cosmin Baciu on 02-Sep-16.
 */
@RunWith(JMockit.class)
public class JMSMessageMapperTest {

    @Tested
    JMSMessageMapper jmsMessageMapper;

    @Test
    public void testConvertJMSMessageSPIList() throws Exception {
        List<InternalJmsMessage> messagesSPIList = new ArrayList<>();
        final InternalJmsMessage internalJmsMessage = new InternalJmsMessage();
        final JmsMessage jmsMessage = new JmsMessage();
        messagesSPIList.add(internalJmsMessage);

        new Expectations(jmsMessageMapper) {{
            jmsMessageMapper.convert(internalJmsMessage);
            result = jmsMessage;
        }};

        List<JmsMessage> convert = jmsMessageMapper.convert(messagesSPIList);
        assertEquals(convert.size(), 1);

        new Verifications() {{
            // Verifies an expected invocation:
            jmsMessageMapper.convert(internalJmsMessage);
            times = 1;
        }};
    }

    @Test
    public void testConvertJmsMessageSPI() throws Exception {
        InternalJmsMessage internalJmsMessage = new InternalJmsMessage();
        internalJmsMessage.setType("myType");
        internalJmsMessage.setId("myid");
        internalJmsMessage.setContent("mycontent");
        Date date = new Date();
        internalJmsMessage.setTimestamp(date);
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("mykey", "myvalue");
        internalJmsMessage.setProperties(messageProperties);

        JmsMessage convert = jmsMessageMapper.convert(internalJmsMessage);
        assertEquals(convert.getType(), "myType");
        assertEquals(convert.getId(), "myid");
        assertEquals(convert.getContent(), "mycontent");
        assertEquals(convert.getTimestamp(), date);
        assertEquals(convert.getProperties().size(), 1);
        assertEquals(convert.getProperties().get("mykey"), "myvalue");
    }

    @Test
    public void testConvertJmsMessage() throws Exception {
        JmsMessage jmsMessage = new JmsMessage();
        jmsMessage.setType("myType");
        jmsMessage.setId("myid");
        jmsMessage.setContent("mycontent");
        Date date = new Date();
        jmsMessage.setTimestamp(date);
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("mykey", "myvalue");
        jmsMessage.setProperties(messageProperties);

        InternalJmsMessage convert = jmsMessageMapper.convert(jmsMessage);
        assertEquals(convert.getType(), "myType");
        assertEquals(convert.getId(), "myid");
        assertEquals(convert.getContent(), "mycontent");
        assertEquals(convert.getTimestamp(), date);
        assertEquals(convert.getProperties().size(), 1);
        assertEquals(convert.getProperties().get("mykey"), "myvalue");
    }

}
