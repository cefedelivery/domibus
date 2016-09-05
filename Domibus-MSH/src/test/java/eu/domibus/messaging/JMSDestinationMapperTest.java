package eu.domibus.messaging;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.jms.spi.JMSDestinationSPI;
import eu.domibus.messaging.jms.JMSDestinationMapper;
import mockit.Expectations;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Cosmin Baciu on 02-Sep-16.
 */
@RunWith(JMockit.class)
public class JMSDestinationMapperTest {

    @Tested
    JMSDestinationMapper destinationMapper;

    @Test
    public void testConvertJMSDestinationMap() throws Exception {
        Map<String, JMSDestinationSPI> destinations = new HashMap<>();
        final JMSDestinationSPI jmsDestinationSPI = new JMSDestinationSPI();
        final JMSDestination jmsDestination = new JMSDestination();
        destinations.put("destinationkey", jmsDestinationSPI);

        new Expectations(destinationMapper) {{
            destinationMapper.convert(jmsDestinationSPI);
            result = jmsDestination;
        }};

        Map<String, JMSDestination> convert = destinationMapper.convert(destinations);
        assertEquals(convert.size(), 1);

        new Verifications() {{
            // Verifies an expected invocation:
            destinationMapper.convert(jmsDestinationSPI);
            times = 1;
        }};
    }

    @Test
    public void testConvertJMSDestination() throws Exception {
        JMSDestinationSPI jmsDestinationSPI = new JMSDestinationSPI();
        jmsDestinationSPI.setType("myType");
        jmsDestinationSPI.setName("myName");
        jmsDestinationSPI.setNumberOfMessages(2);
        jmsDestinationSPI.setInternal(true);
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("mykey", "myvalue");
        jmsDestinationSPI.setProperties(messageProperties);

        JMSDestination convert = destinationMapper.convert(jmsDestinationSPI);
        assertEquals(convert.getType(), "myType");
        assertEquals(convert.getName(), "myName");
        assertEquals(convert.getNumberOfMessages(), 2);
        assertEquals(convert.isInternal(), true);
        assertEquals(convert.getProperties().size(), 1);
        assertEquals(convert.getProperties().get("mykey"), "myvalue");
    }

}
