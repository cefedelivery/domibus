package eu.domibus.messaging;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.jms.spi.JMSDestinationSPI;
import eu.domibus.jms.spi.JMSManagerSPI;
import eu.domibus.jms.spi.JmsMessageSPI;
import eu.domibus.messaging.jms.JMSDestinationMapper;
import eu.domibus.messaging.jms.JMSManagerImpl;
import eu.domibus.messaging.jms.JMSMessageMapper;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.util.*;

/**
 * Created by Cosmin Baciu on 02-Sep-16.
 */
@RunWith(JMockit.class)
public class JMSManagerImplTest {

    @Tested
    JMSManagerImpl jmsManager;

    @Injectable
    JMSManagerSPI jmsManagerSPI;

    @Injectable
    JMSDestinationMapper jmsDestinationMapper;

    @Injectable
    JMSMessageMapper jmsMessageMapper;

    @Test
    public void testGetDestinations() throws Exception {
        final Map<String, JMSDestinationSPI> destinations = new HashMap<>();

        new Expectations() {{
            jmsManagerSPI.getDestinations();
            result = destinations;
        }};

        jmsManager.getDestinations();

        new Verifications() {{
            jmsDestinationMapper.convert(destinations);
            times = 1;
        }};
    }

    @Test
    public void testGetMessage() throws Exception {
        final String source = "source";
        final String messageId = "messageId";
        final JmsMessageSPI jmsMessageSPI = new JmsMessageSPI();

        new Expectations() {{
            jmsManagerSPI.getMessage(source, messageId);
            result = jmsMessageSPI;
        }};

        jmsManager.getMessage(source, messageId);

        new Verifications() {{
            jmsMessageMapper.convert(jmsMessageSPI);
            times = 1;
        }};
    }

    @Test
    public void testGetMessages() throws Exception {
        final String source = "source";
        final String jmsType = "jmsType";
        final Date fromDate = new Date();
        final Date toDate = new Date();
        final String selector = "myselector";
        final String messageId = "messageId";
        final List<JmsMessageSPI> jmsMessageSPI = new ArrayList<>();

        new Expectations() {{
            jmsManagerSPI.getMessages(source, jmsType, fromDate, toDate, selector);
            result = jmsMessageSPI;
        }};

        jmsManager.getMessages(source, jmsType, fromDate, toDate, selector);

        new Verifications() {{
            jmsMessageMapper.convert(jmsMessageSPI);
        }};
    }

    @Test
    public void testSendMessageToQueue() throws Exception {
        final JmsMessage message = new JmsMessage();
        final JmsMessageSPI messageSPI = new JmsMessageSPI();

        new Expectations() {{
            jmsMessageMapper.convert(message);
            result = messageSPI;
        }};

        jmsManager.sendMessageToQueue(message, "myqueue");

        new Verifications() {{
            jmsMessageMapper.convert(message);
            times = 1;

            jmsManagerSPI.sendMessage(messageSPI, "myqueue");

            Assert.assertEquals(message.getProperty(JmsMessage.PROPERTY_ORIGINAL_QUEUE), "myqueue");
        }};
    }

    @Test
    public void testSendMessageToJmsQueue(@Injectable final Queue queue) throws Exception {
        final JmsMessage message = new JmsMessage();
        final JmsMessageSPI messageSPI = new JmsMessageSPI();


        new Expectations() {{
            jmsMessageMapper.convert(message);
            result = messageSPI;

            queue.getQueueName();
            result = "myqueue";
        }};

        jmsManager.sendMessageToQueue(message, queue);

        new Verifications() {{
            jmsMessageMapper.convert(message);
            times = 1;

            jmsManagerSPI.sendMessage(messageSPI, queue);

            Assert.assertEquals(message.getProperty(JmsMessage.PROPERTY_ORIGINAL_QUEUE), "myqueue");
        }};
    }

    @Test
    public void testDeleteMessages() throws Exception {
        final String source = "myqueue";
        final String[] messageIds = new String[] {"1", "2"};

        jmsManager.deleteMessages(source, messageIds);

        new Verifications() {{
            jmsManagerSPI.deleteMessages(source, messageIds);
        }};
    }

    @Test
    public void testMoveMessages(@Injectable final Queue queue) throws Exception {
        final String source = "myqueue";
        final String destination = "destinationQueue";
        final String[] messageIds = new String[] {"1", "2"};

        jmsManager.moveMessages(source, destination, messageIds);

        new Verifications() {{
            jmsManagerSPI.moveMessages(source, destination, messageIds);
        }};
    }
}
