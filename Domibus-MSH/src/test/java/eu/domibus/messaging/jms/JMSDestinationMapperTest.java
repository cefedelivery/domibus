package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.jms.spi.InternalJMSDestination;
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
 * @author Cosmin Baciu
 * @since 3.2
 */
@RunWith(JMockit.class)
public class JMSDestinationMapperTest {

    @Tested
    JMSDestinationMapper destinationMapper;

    @Test
    public void testConvertJMSDestinationMap() throws Exception {
        Map<String, InternalJMSDestination> destinations = new HashMap<>();
        final InternalJMSDestination internalJmsDestination = new InternalJMSDestination();
        final JMSDestination jmsDestination = new JMSDestination();
        destinations.put("destinationkey", internalJmsDestination);

        new Expectations(destinationMapper) {{
            destinationMapper.convert(internalJmsDestination);
            result = jmsDestination;
        }};

        Map<String, JMSDestination> convert = destinationMapper.convert(destinations);
        assertEquals(convert.size(), 1);

        new Verifications() {{
            // Verifies an expected invocation:
            destinationMapper.convert(internalJmsDestination);
            times = 1;
        }};
    }

    @Test
    public void testConvertJMSDestination() throws Exception {
        InternalJMSDestination internalJmsDestination = new InternalJMSDestination();
        internalJmsDestination.setType("myType");
        internalJmsDestination.setName("myName");
        internalJmsDestination.setNumberOfMessages(2);
        internalJmsDestination.setInternal(true);
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("mykey", "myvalue");
        internalJmsDestination.setProperties(messageProperties);

        JMSDestination convert = destinationMapper.convert(internalJmsDestination);
        assertEquals(convert.getType(), "myType");
        assertEquals(convert.getName(), "myName");
        assertEquals(convert.getNumberOfMessages(), 2);
        assertEquals(convert.isInternal(), true);
        assertEquals(convert.getProperties().size(), 1);
        assertEquals(convert.getProperties().get("mykey"), "myvalue");
    }

}
