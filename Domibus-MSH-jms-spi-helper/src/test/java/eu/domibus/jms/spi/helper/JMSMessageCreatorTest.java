package eu.domibus.jms.spi.helper;

import eu.domibus.jms.spi.InternalJmsMessage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@RunWith(JMockit.class)
public class JMSMessageCreatorTest {

    @Test
    public void testCreateMessage(@Injectable final Session session, @Injectable final TextMessage result) throws Exception {
        final InternalJmsMessage internalJmsMessage = new InternalJmsMessage();
        internalJmsMessage.setContent("mycontent");
        internalJmsMessage.setType("mytype");

        final Map<String, Object> properties = new HashMap<>();
        properties.put("key1", "value1");
        internalJmsMessage.setProperties(properties);

        new Expectations(internalJmsMessage) {{
            internalJmsMessage.getCustomProperties();
            result = properties;

            session.createTextMessage();
        }};

        JmsMessageCreator jmsMessageCreator = new JmsMessageCreator(internalJmsMessage);
        jmsMessageCreator.createMessage(session);

        new Verifications() {{
            result.setText("mycontent");
            result.setJMSType("mytype");
            result.setObjectProperty("key1", "value1");
        }};
    }
}
