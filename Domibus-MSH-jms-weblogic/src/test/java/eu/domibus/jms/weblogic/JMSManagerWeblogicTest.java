package eu.domibus.jms.weblogic;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.jms.spi.InternalJMSDestination;
import eu.domibus.jms.spi.InternalJmsMessage;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.naming.InitialContext;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Cosmin Baciu on 30-Sep-16.
 */
@RunWith(JMockit.class)
public class JMSManagerWeblogicTest {

    @Tested
    InternalJMSManagerWeblogic jmsManagerWeblogic;

    @Injectable
    JMXHelper jmxHelper;

    @Injectable
    JMXTemplate jmxTemplate;

    @Injectable
    private JmsOperations jmsSender;

    @Injectable
    JMSDestinationHelper jmsDestinationHelper;

    @Injectable
    JMSSelectorUtil jmsSelectorUtil;

    @Test
    public void testGetQueueName() throws Exception {
        String queueName = jmsManagerWeblogic.getShortDestName("JmsModule!DomibusNotifyBackendEtrustexQueue");
        Assert.assertEquals(queueName, "DomibusNotifyBackendEtrustexQueue");

        queueName = jmsManagerWeblogic.getShortDestName("DomibusNotifyBackendEtrustexQueue");
        Assert.assertEquals(queueName, "DomibusNotifyBackendEtrustexQueue");
    }


    @Test
    public void testGetMessagesFromDestinationNOk(@Mocked final ObjectName destination,
                                                  final @Injectable MBeanServerConnection mbsc,
                                                  final @Injectable CompositeData data1,
                                                  final @Injectable CompositeData data2,
                                                  final @Injectable InternalJmsMessage internalJmsMessage1) throws Exception {
        final String selector = "";
        final CompositeData[] compositeDatas = new CompositeData[]{data1, data2};

        new Expectations(jmsManagerWeblogic) {{
            mbsc.invoke(destination, "getCursorSize", withAny(new Object[]{""}), withAny(new String[]{String.class.getName()}));
            result = 2L;

            mbsc.invoke(destination, "getItems", withAny(new Object[]{"", new Long(0),
                    new Integer(2)}), withAny(new String[]{String.class.getName(), Long.class.getName(), Integer.class.getName()}));
            result = compositeDatas;

            jmsManagerWeblogic.getInternalJmsMessage(destination, mbsc, anyString, data1);
            result = new RuntimeException("Simulating a message conversion error");

            jmsManagerWeblogic.getInternalJmsMessage(destination, mbsc, anyString, data2);
            result = internalJmsMessage1;
        }};

        try {
            final List<InternalJmsMessage> internalJmsMessages = jmsManagerWeblogic.doGetMessagesFromDestination(mbsc, selector, destination);
        } catch (RuntimeException runEx) {
            assertEquals("Simulating a message conversion error", runEx.getMessage());
        }
    }

    @Test
    public void testGetQueueMap(@Mocked final ObjectName drs,
                                @Mocked final ObjectName config,
                                @Mocked final ObjectName configJmsResource,
                                @Mocked final ObjectName configJmsSystemResource,
                                @Mocked final ObjectName configQueue,
                                @Mocked final ObjectName distributedQueue,
                                @Mocked final ObjectName uniformDistributedQueue,
                                final @Injectable MBeanServerConnection mbsc) throws Exception {
        final ObjectName[] configJmsSystemResources = new ObjectName[]{configJmsSystemResource};
        final ObjectName[] configQueues = new ObjectName[]{configQueue};
        final ObjectName[] distributedQueues = new ObjectName[]{distributedQueue};
        final ObjectName[] uniformDistributedQueues = new ObjectName[]{uniformDistributedQueue};

        new Expectations(jmsManagerWeblogic) {{
            jmxHelper.getDomainRuntimeService();
            result = drs;

            mbsc.getAttribute(drs, "DomainConfiguration");
            result = config;

            mbsc.getAttribute(config, "JMSSystemResources");
            result = configJmsSystemResources;

            mbsc.getAttribute(configJmsSystemResource, "JMSResource");
            result = configJmsResource;

            mbsc.getAttribute(configJmsResource, "Queues");
            result = configQueues;

            mbsc.getAttribute(configQueue, "Name");
            result = "myqueue";

            mbsc.getAttribute(configJmsResource, "DistributedQueues");
            result = distributedQueues;

            mbsc.getAttribute(distributedQueue, "Name");
            result = "mydistributedQueue";

            mbsc.getAttribute(configJmsResource, "UniformDistributedQueues");
            result = uniformDistributedQueues;

            mbsc.getAttribute(uniformDistributedQueue, "Name");
            result = "myuniformDistributedQueue";

        }};

        final Map<String, ObjectName> queueMap = jmsManagerWeblogic.getQueueMap(mbsc);
        assertNotNull(queueMap);
        assertEquals(queueMap.size(), 3);
    }

    @Test
    public void testGetDestinations(@Mocked final ObjectName drs,
                                    @Mocked final ObjectName server1,
                                    @Mocked final ObjectName jmsRuntime,
                                    @Mocked final ObjectName jmsServer,
                                    @Mocked final ObjectName jmsDestination,
                                    @Mocked final ObjectName configQueue,
                                    final @Injectable MBeanServerConnection mbsc) throws Exception {
        final ObjectName[] servers = new ObjectName[]{server1};
        final ObjectName[] jmsServers = new ObjectName[]{jmsServer};
        final ObjectName[] jmsDestinations = new ObjectName[]{jmsDestination};
        final Map<String, ObjectName> queueMap = new HashMap<>();
        final String queueName = "myqueue";
        queueMap.put(queueName, configQueue);


        new Expectations(jmsManagerWeblogic) {{
            ObjectName drs = jmxHelper.getDomainRuntimeService();
            result = drs;

            mbsc.getAttribute(drs, "ServerRuntimes");
            result = servers;

            mbsc.getAttribute(server1, "JMSRuntime");
            result = jmsRuntime;

            mbsc.getAttribute(jmsRuntime, "JMSServers");
            result = jmsServers;

            mbsc.getAttribute(jmsServer, "Destinations");
            result = jmsDestinations;

            mbsc.getAttribute(jmsDestination, "Name");
            result = queueName;

            jmsManagerWeblogic.getQueueMap(mbsc);
            result = queueMap;

            mbsc.getAttribute(configQueue, "JNDIName");
            result = "jndi/myqueue";

            mbsc.getAttribute(jmsDestination, "MessagesCurrentCount");
            result = 2L;

        }};

        final Map<String, InternalJMSDestination> destinations = jmsManagerWeblogic.findDestinationsGroupedByFQName(mbsc);
        assertNotNull(destinations);
        final InternalJMSDestination internalJmsDestination = destinations.get(queueName);
        assertNotNull(internalJmsDestination);

        assertEquals(internalJmsDestination.getName(), queueName);
        assertEquals(internalJmsDestination.getProperty("ObjectName"), jmsDestination);
        assertEquals(internalJmsDestination.getProperty("Jndi"), "jndi/myqueue");
        assertEquals(internalJmsDestination.getNumberOfMessages(), 2L);

    }

    @Test
    public void testConvertMessage(final @Injectable CompositeData data) throws Exception {
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("jms/WebLogicJMSMessageXML.xml");
        final String jmsMessageXML = IOUtils.toString(xmlStream);

        new Expectations() {{
            data.get("MessageXMLText");
            result = jmsMessageXML;

        }};

        InternalJmsMessage internalJmsMessage = jmsManagerWeblogic.convertMessage(data);

        assertEquals(internalJmsMessage.getTimestamp().getTime(), 1481721027366L);
        assertEquals(internalJmsMessage.getId(), "ID:<704506.1481721027366.0>");
        assertEquals(internalJmsMessage.getContent(), "mycontent");
        assertEquals(internalJmsMessage.getType(), "myJMSType");

        Map<String, Object> properties = internalJmsMessage.getProperties();
        assertEquals(properties.get("JMSType"), "myJMSType");
        assertEquals(properties.get("originalQueue"), "DomibusErrorNotifyProducerQueue");
    }


    @Test
    public void testBrowseMessagesMessages(final @Injectable List<InternalJMSDestination> internalJmsDestinations,
                                           final @Injectable InternalJMSDestination internalJmsDestination,
                                           final @Mocked ObjectName destination,
                                           final @Injectable List<InternalJmsMessage> messageSPIs) throws Exception {
        final String source = "myqueue";
        final String jmsType = "message";
        final Date fromDate = new Date();
        final Date toDate = new Date();
        final String selectorClause = "mytype = 'message'";

        new Expectations(jmsManagerWeblogic) {{

            jmsManagerWeblogic.getInternalJMSDestinations(source);
            result = internalJmsDestinations;

            internalJmsDestination.getType();
            result = "Queue";

            jmsSelectorUtil.getSelector(withAny(new HashMap<String, Object>()));
            result = null;

            internalJmsDestination.getProperty("ObjectName");
            result = destination;

            jmsManagerWeblogic.getMessagesFromDestination(destination, anyString);
            result = messageSPIs;
        }};


        List<InternalJmsMessage> messages = jmsManagerWeblogic.browseMessages(source, jmsType, fromDate, toDate, selectorClause);
        assertEquals(messages, messageSPIs);

        new Verifications() {{
            Map<String, Object> criteria = new HashMap<>();
            jmsSelectorUtil.getSelector(criteria = withCapture());

            assertEquals(criteria.get("JMSType"), jmsType);
            assertEquals(criteria.get("JMSTimestamp_from"), fromDate.getTime());
            assertEquals(criteria.get("JMSTimestamp_to"), toDate.getTime());
            assertEquals(criteria.get("selectorClause"), selectorClause);
        }};
    }

    @Test
    public void testGetMessage(final @Injectable InternalJMSDestination internalJmsDestination,
                               final @Injectable List<InternalJMSDestination> internalJmsDestinations,
                               final @Injectable ObjectName destination) throws Exception {
        final String myqueue = "myqueue";
        final String messageId = "id1";

        new Expectations(jmsManagerWeblogic) {{
            jmsManagerWeblogic.getInternalJMSDestinations(myqueue);
            result = internalJmsDestination;

            internalJmsDestination.getProperty("ObjectName");
            result = destination;

            jmsManagerWeblogic.getMessageFromDestination(withAny(destination), anyString);
            result = null;
        }};


        jmsManagerWeblogic.getMessage(myqueue, messageId);

        new Verifications() {{
            String capturedMessageId = null;
            ObjectName capturedDestination = null;

            jmsManagerWeblogic.getMessageFromDestination(capturedDestination = withCapture(), capturedMessageId = withCapture());
            assertTrue(destination == capturedDestination);
            assertEquals(messageId, capturedMessageId);
        }};
    }

    @Test
    public void testMoveMessages(final @Injectable InternalJMSDestination internalJmsDestinationSource,
                                 final @Injectable InternalJMSDestination internalJmsDestinationSPIDestination,
                                 final @Injectable List<InternalJMSDestination> internalJmsDestinations,
                                 final @Injectable ObjectName sourceObjectName,
                                 final @Injectable ObjectName destinationObjectName) throws Exception {

        final String sourceQueue = "sourceQueue";
        final String destinationQueue = "destinationQueue";
        final String[] messageIds = new String[]{"1"};

        new Expectations(jmsManagerWeblogic) {{
            jmsManagerWeblogic.getInternalJMSDestinations(sourceQueue);
            result = internalJmsDestinationSource;

            jmsManagerWeblogic.getInternalJMSDestinations(destinationQueue);
            result = internalJmsDestinationSPIDestination;

            internalJmsDestinationSource.getProperty("ObjectName");
            result = sourceObjectName;

            internalJmsDestinationSPIDestination.getProperty("ObjectName");
            result = destinationObjectName;

            jmsManagerWeblogic.moveMessages(sourceObjectName, destinationObjectName, anyString);
            result = 1;

            jmsSelectorUtil.getSelector(messageIds);
            result = "myselector";
        }};

        jmsManagerWeblogic.moveMessages(sourceQueue, destinationQueue, messageIds);

        new Verifications() {{
            ObjectName capturedSource = null;
            ObjectName capturedDestination = null;
            String capturedSelector = null;

            jmsManagerWeblogic.moveMessages(capturedSource = withCapture(), capturedDestination = withCapture(), capturedSelector = withCapture());
            assertTrue(capturedSource == sourceObjectName);
            assertTrue(capturedDestination == destinationObjectName);
            assertEquals(capturedSelector, "myselector");
        }};
    }

    @Test
    public void testDeleteMessages(final @Injectable InternalJMSDestination internalJmsDestinationSource,
                                   final @Injectable List<InternalJMSDestination> internalJmsDestinations,
                                   final @Injectable ObjectName sourceObjectName) throws Exception {

        final String sourceQueue = "sourceQueue";
        final String[] messageIds = new String[]{"1"};

        new Expectations(jmsManagerWeblogic) {{
            jmsManagerWeblogic.getInternalJMSDestinations(sourceQueue);
            result = internalJmsDestinationSource;

            internalJmsDestinationSource.getProperty("ObjectName");
            result = sourceObjectName;

            jmsManagerWeblogic.deleteMessages(sourceObjectName, anyString);
            result = 1;

            jmsSelectorUtil.getSelector(messageIds);
            result = "myselector";
        }};

        jmsManagerWeblogic.deleteMessages(sourceQueue, messageIds);

        new Verifications() {{
            ObjectName capturedSource = null;
            String capturedSelector = null;

            jmsManagerWeblogic.deleteMessages(capturedSource = withCapture(), capturedSelector = withCapture());
            assertTrue(capturedSource == sourceObjectName);
            assertEquals(capturedSelector, "myselector");
        }};
    }

    @Test
    public void testSendMessage(final @Injectable InternalJmsMessage internalJmsMessage,
                                final @Injectable InternalJMSDestination internalJmsDestination,
                                final @Mocked javax.jms.Queue jmsDestination) throws Exception {

        final String destinationQueue = "destinationQueue";
        final String jndi = "jndiqueue";

        new Expectations(jmsManagerWeblogic) {{

            jmsManagerWeblogic.findDestinationsGroupedByName().get(destinationQueue).get(0);
            result = internalJmsDestination;

            internalJmsDestination.getProperty("Jndi");
            result = jndi;

            new MockUp<InitialContext>() {
                @Mock
                javax.jms.Queue doLookup(String input) {
                    return jmsDestination;
                }
            };

        }};

        jmsManagerWeblogic.sendMessage(internalJmsMessage, destinationQueue);

        new Verifications() {{
            InternalJmsMessage capturedInternalJmsMessage = null;
            javax.jms.Queue capturedQueue = null;

            jmsManagerWeblogic.sendMessage(capturedInternalJmsMessage = withCapture(), capturedQueue = withCapture());
            assertTrue(capturedInternalJmsMessage == internalJmsMessage);
            assertTrue(capturedQueue == jmsDestination);
        }};
    }
}
