package eu.domibus.jms.spi.helper;

import eu.domibus.jms.spi.JmsMessageSPI;
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
 * Created by Cosmin Baciu on 02-Sep-16.
 */
@RunWith(JMockit.class)
public class JMSMessageCreatorTest {

    @Test
    public void testCreateMessage(@Injectable final Session session, @Injectable final TextMessage result) throws Exception {
        final JmsMessageSPI jmsMessageSPI = new JmsMessageSPI();
        jmsMessageSPI.setContent("mycontent");
        jmsMessageSPI.setType("mytype");

        final Map<String, Object> properties = new HashMap<>();
        properties.put("key1", "value1");
        jmsMessageSPI.setProperties(properties);

        new Expectations(jmsMessageSPI) {{
            jmsMessageSPI.getCustomProperties();
            result = properties;

            session.createTextMessage();
        }};

        JmsMessageCreator jmsMessageCreator = new JmsMessageCreator(jmsMessageSPI);
        jmsMessageCreator.createMessage(session);

        new Verifications() {{
            result.setText("mycontent");
            result.setJMSType("mytype");
            result.setObjectProperty("key1", "value1");
        }};
    }
}
