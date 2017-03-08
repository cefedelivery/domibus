package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.jms.spi.InternalJMSDestination;
import eu.domibus.jms.spi.InternalJMSManager;
import eu.domibus.jms.spi.InternalJmsMessage;
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
 * @author Cosmin Baciu
 * @since 3.2
 */
@RunWith(JMockit.class)
public class JMSManagerImplTest {

    @Tested
    JMSManagerImpl jmsManager;

    @Injectable
    InternalJMSManager internalJmsManager;

    @Injectable
    JMSDestinationMapper jmsDestinationMapper;

    @Injectable
    JMSMessageMapper jmsMessageMapper;

    @Test
    public void testGetDestinations() throws Exception {

        final Map<String, InternalJMSDestination> destinations = new HashMap<>();

        new Expectations() {{
            internalJmsManager.findDestinationsGroupedByFQName();
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
        final InternalJmsMessage internalJmsMessage = new InternalJmsMessage();

        new Expectations() {{
            internalJmsManager.getMessage(source, messageId);
            result = internalJmsMessage;
        }};

        jmsManager.getMessage(source, messageId);

        new Verifications() {{
            jmsMessageMapper.convert(internalJmsMessage);
            times = 1;
        }};
    }

    @Test
    public void testBrowseMessages() throws Exception {
        final String source = "source";
        final String jmsType = "jmsType";
        final Date fromDate = new Date();
        final Date toDate = new Date();
        final String selector = "myselector";
        final List<InternalJmsMessage> internalJmsMessage = new ArrayList<>();

        new Expectations() {{
            internalJmsManager.browseMessages(source, jmsType, fromDate, toDate, selector);
            result = internalJmsMessage;
        }};

        jmsManager.browseMessages(source, jmsType, fromDate, toDate, selector);

        new Verifications() {{
            jmsMessageMapper.convert(internalJmsMessage);
        }};
    }

    @Test
    public void testSendMessageToQueue() throws Exception {
        final JmsMessage message = new JmsMessage();
        final InternalJmsMessage messageSPI = new InternalJmsMessage();

        new Expectations() {{
            jmsMessageMapper.convert(message);
            result = messageSPI;
        }};

        jmsManager.sendMessageToQueue(message, "myqueue");

        new Verifications() {{
            jmsMessageMapper.convert(message);
            times = 1;

            internalJmsManager.sendMessage(messageSPI, "myqueue");

            Assert.assertEquals(message.getProperty(JmsMessage.PROPERTY_ORIGINAL_QUEUE), "myqueue");
        }};
    }

    @Test
    public void testSendMessageToJmsQueue(@Injectable final Queue queue) throws Exception {
        final JmsMessage message = new JmsMessage();
        final InternalJmsMessage messageSPI = new InternalJmsMessage();


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

            internalJmsManager.sendMessage(messageSPI, queue);

            Assert.assertEquals(message.getProperty(JmsMessage.PROPERTY_ORIGINAL_QUEUE), "myqueue");
        }};
    }

    @Test
    public void testDeleteMessages() throws Exception {
        final String source = "myqueue";
        final String[] messageIds = new String[] {"1", "2"};

        jmsManager.deleteMessages(source, messageIds);

        new Verifications() {{
            internalJmsManager.deleteMessages(source, messageIds);
        }};
    }

    @Test
    public void testMoveMessages(@Injectable final Queue queue) throws Exception {
        final String source = "myqueue";
        final String destination = "destinationQueue";
        final String[] messageIds = new String[] {"1", "2"};

        jmsManager.moveMessages(source, destination, messageIds);

        new Verifications() {{
            internalJmsManager.moveMessages(source, destination, messageIds);
        }};
    }
}
