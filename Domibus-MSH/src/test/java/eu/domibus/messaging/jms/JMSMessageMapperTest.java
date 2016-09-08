package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.jms.spi.JmsMessageSPI;
import eu.domibus.messaging.jms.JMSMessageMapper;
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
        List<JmsMessageSPI> messagesSPIList = new ArrayList<>();
        final JmsMessageSPI jmsMessageSPI = new JmsMessageSPI();
        final JmsMessage jmsMessage = new JmsMessage();
        messagesSPIList.add(jmsMessageSPI);

        new Expectations(jmsMessageMapper) {{
            jmsMessageMapper.convert(jmsMessageSPI);
            result = jmsMessage;
        }};

        List<JmsMessage> convert = jmsMessageMapper.convert(messagesSPIList);
        assertEquals(convert.size(), 1);

        new Verifications() {{
            // Verifies an expected invocation:
            jmsMessageMapper.convert(jmsMessageSPI);
            times = 1;
        }};
    }

    @Test
    public void testConvertJmsMessageSPI() throws Exception {
        JmsMessageSPI jmsMessageSPI = new JmsMessageSPI();
        jmsMessageSPI.setType("myType");
        jmsMessageSPI.setId("myid");
        jmsMessageSPI.setContent("mycontent");
        Date date = new Date();
        jmsMessageSPI.setTimestamp(date);
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("mykey", "myvalue");
        jmsMessageSPI.setProperties(messageProperties);

        JmsMessage convert = jmsMessageMapper.convert(jmsMessageSPI);
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

        JmsMessageSPI convert = jmsMessageMapper.convert(jmsMessage);
        assertEquals(convert.getType(), "myType");
        assertEquals(convert.getId(), "myid");
        assertEquals(convert.getContent(), "mycontent");
        assertEquals(convert.getTimestamp(), date);
        assertEquals(convert.getProperties().size(), 1);
        assertEquals(convert.getProperties().get("mykey"), "myvalue");
    }

}
