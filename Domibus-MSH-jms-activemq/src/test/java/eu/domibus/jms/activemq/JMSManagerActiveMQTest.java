package eu.domibus.jms.activemq;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.jms.spi.InternalJMSDestination;
import eu.domibus.jms.spi.InternalJmsMessage;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 */
@RunWith(JMockit.class)
public class JMSManagerActiveMQTest {

    @Tested
    InternalJMSManagerActiveMQ jmsManagerActiveMQ;

    @Injectable
    MBeanServerConnection mBeanServerConnection;

    @Injectable
    BrokerViewMBean brokerViewMBean;

    @Injectable
    JMSDestinationHelper jmsDestinationHelper;

    @Injectable
    private JmsOperations jmsOperations;

    @Injectable
    JMSSelectorUtil jmsSelectorUtil;

    @Test
    public void testGetDestinations(final @Mocked ObjectName objectName1,
                                    final @Mocked ObjectName objectName2,
                                    final @Injectable QueueViewMBean queueMbean1,
                                    final @Injectable QueueViewMBean queueMbean2,
                                    final @Injectable InternalJMSDestination internalJmsDestination1,
                                    final @Injectable InternalJMSDestination internalJmsDestination2) throws Exception {
        final Map<String, ObjectName> objectNameMap = new HashMap<>();
        objectNameMap.put("queue1", objectName1);
        objectNameMap.put("queue2", objectName2);
        new Expectations(jmsManagerActiveMQ) {{
            jmsManagerActiveMQ.getQueueMap();
            result = objectNameMap;

            jmsManagerActiveMQ.getQueue(objectName1);
            result = queueMbean1;

            jmsManagerActiveMQ.getQueue(objectName2);
            result = queueMbean2;

            jmsManagerActiveMQ.createInternalJmsDestination(objectName1, queueMbean1);
            result = internalJmsDestination1;

            jmsManagerActiveMQ.createInternalJmsDestination(objectName2, queueMbean2);
            result = internalJmsDestination2;

            queueMbean1.getName();
            result = "queueMbean1";

            queueMbean2.getName();
            result = "queueMbean2";
        }};

        Map<String, InternalJMSDestination> destinations = jmsManagerActiveMQ.getDestinations();
        assertNotNull(destinations);
        assertEquals(destinations.size(), 2);

        assertEquals(destinations.get("queueMbean1"), internalJmsDestination1);
        assertEquals(destinations.get("queueMbean2"), internalJmsDestination2);
    }

    @Test
    public void testCreateJmsDestinationSPI(final @Mocked ObjectName objectName,
                                            final @Injectable QueueViewMBean queueMbean) throws Exception {
        final Map<String, ObjectName> objectNameMap = new HashMap<>();
        objectNameMap.put("queue1", objectName);
        new Expectations(jmsManagerActiveMQ) {{
            queueMbean.getName();
            result = "queueMbean1";

            jmsDestinationHelper.isInternal(queueMbean.getName());
            result = true;
        }};

        InternalJMSDestination internalJmsDestination = jmsManagerActiveMQ.createInternalJmsDestination(objectName, queueMbean);
        assertEquals(internalJmsDestination.getName(), queueMbean.getName());
        assertEquals(internalJmsDestination.isInternal(), jmsDestinationHelper.isInternal(queueMbean.getName()));
        assertEquals(internalJmsDestination.getType(), InternalJMSDestination.QUEUE_TYPE);
        assertEquals(internalJmsDestination.getNumberOfMessages(), queueMbean.getQueueSize());
        assertEquals(internalJmsDestination.getProperty("ObjectName"), objectName);
    }

    @Test
    public void testGetQueue(final @Mocked ObjectName objectName,
                             final @Injectable QueueViewMBean queueMbean) throws Exception {
        new Expectations(MBeanServerInvocationHandler.class) {{
            MBeanServerInvocationHandler.newProxyInstance(mBeanServerConnection, objectName, QueueViewMBean.class, true);
            result = queueMbean;
        }};

        QueueViewMBean queue = jmsManagerActiveMQ.getQueue(objectName);
        assertEquals(queue, queueMbean);
    }

    @Test
    public void testGetQueueMapWhenAlreadyInstantiated(final @Mocked ObjectName objectName,
                                                       final @Injectable QueueViewMBean queueMbean,
                                                       final @Injectable Map<String, ObjectName> queueMap) throws Exception {
        Map<String, ObjectName> returnedQueueMap = jmsManagerActiveMQ.getQueueMap();
        assertEquals(returnedQueueMap, queueMap);

        new Verifications() {{
            brokerViewMBean.getQueues();
            times = 0;
        }};
    }

    @Test
    public void testGetQueueMapWhenNotInstantiated(final @Mocked ObjectName objectName,
                                                   final @Injectable QueueViewMBean queueMbean) throws Exception {
        new Expectations(jmsManagerActiveMQ) {{
            brokerViewMBean.getQueues();
            result = objectName;

            jmsManagerActiveMQ.getQueue(objectName);
            result = queueMbean;

            queueMbean.getName();
            result = "queueMbean1";
        }};

        Map<String, ObjectName> queueMap = jmsManagerActiveMQ.getQueueMap();

        new Verifications() {{
            brokerViewMBean.getQueues();
        }};

        assertNotNull(queueMap);
        assertEquals(queueMap.size(), 1);
        assertEquals(queueMap.get("queueMbean1"), objectName);
    }

    @Test
    public void testGetMessages(final @Injectable InternalJMSDestination selectedDestination,
                                final @Injectable QueueViewMBean queueMbean,
                                final @Injectable CompositeData[] compositeDatas,
                                final @Injectable Map<String, InternalJMSDestination> destinationMap,
                                final @Injectable List<InternalJmsMessage> messageSPIs) throws Exception {
        final String source = "myqueue";
        final String jmsType = "message";
        final Date fromDate = new Date();
        final Date toDate = new Date();
        final String selectorClause = "mytype = 'message'";

        new Expectations(jmsManagerActiveMQ) {{
            jmsManagerActiveMQ.getDestinations().get(source);
            result = destinationMap;
            destinationMap.get(source);
            result = selectedDestination;

            selectedDestination.getType();
            result = "Queue";

            jmsSelectorUtil.getSelector(withAny(new HashMap<String, Object>()));
            result = null;

            jmsManagerActiveMQ.getQueue(source);
            result = queueMbean;
            queueMbean.browse(anyString);
            result = compositeDatas;
            jmsManagerActiveMQ.convertCompositeData(compositeDatas);
            result = messageSPIs;
        }};


        List<InternalJmsMessage> messages = jmsManagerActiveMQ.getMessages(source, jmsType, fromDate, toDate, selectorClause);
        assertEquals(messages, messageSPIs);

        new Verifications() {{
            Map<String, Object> criteria = null;
            jmsSelectorUtil.getSelector(criteria = withCapture());

            assertEquals(criteria.get("JMSType"), jmsType);
            assertEquals(criteria.get("JMSTimestamp_from"), fromDate.getTime());
            assertEquals(criteria.get("JMSTimestamp_to"), toDate.getTime());
            assertEquals(criteria.get("selectorClause"), selectorClause);

            jmsManagerActiveMQ.getQueue(source);
            queueMbean.browse(anyString);
            jmsManagerActiveMQ.convertCompositeData(compositeDatas);
        }};
    }

    @Test
    public void testConvertCompositeData(final @Injectable CompositeData data,
                                         final @Injectable Map stringProperties,
                                         final @Injectable CompositeDataSupport dataSupport1,
                                         final @Injectable CompositeDataSupport dataSupport2) throws Exception {
        final String jmsType = "mytype";
        final Date jmsTimestamp = new Date();
        final String jmsId1 = "jmsId1";
        final String textMessage = "textmessage";

        new Expectations(jmsManagerActiveMQ) {
            {
                data.get("JMSType");
                result = jmsType;

                data.get("JMSTimestamp");
                result = jmsTimestamp;

                data.get("JMSMessageID");
                result = jmsId1;

                data.containsKey("Text");
                result = true;

                data.get("Text");
                result = textMessage;

                Set<String> allPropertyNames = new HashSet<>();
                allPropertyNames.add("JMSProp1");
                data.getCompositeType().keySet();
                result = allPropertyNames;

                data.get("JMSProp1");
                result = "JMSValue1";

                data.get("StringProperties");
                result = stringProperties;

                dataSupport1.get("key");
                result = "key1";
                dataSupport1.get("value");
                result = "value1";

                dataSupport2.get("key");
                result = "key2";
                dataSupport2.get("value");
                result = "value2";

                stringProperties.values();
                result = Arrays.asList(new CompositeDataSupport[]{dataSupport1, dataSupport2});
            }
        };


        InternalJmsMessage internalJmsMessage = jmsManagerActiveMQ.convertCompositeData(data);

        assertEquals(internalJmsMessage.getType(), jmsType);
        assertEquals(internalJmsMessage.getTimestamp(), jmsTimestamp);
        assertEquals(internalJmsMessage.getId(), jmsId1);
        assertEquals(internalJmsMessage.getContent(), textMessage);

        Map<String, Object> properties = internalJmsMessage.getProperties();
        assertEquals(properties.size(), 3);
        assertEquals(properties.get("key1"), "value1");
        assertEquals(properties.get("key2"), "value2");
        assertEquals(properties.get("JMSProp1"), "JMSValue1");
    }

    @Test
    public void testConvertCompositeDataArray(final @Injectable CompositeData data1,
                                              final @Injectable InternalJmsMessage internalJmsMessage1) throws Exception {
        CompositeData[] compositeDatas = new CompositeData[]{data1};

        new Expectations(jmsManagerActiveMQ) {{
            jmsManagerActiveMQ.convertCompositeData(data1);
            result = internalJmsMessage1;
        }};

        final List<InternalJmsMessage> internalJmsMessages = jmsManagerActiveMQ.convertCompositeData(compositeDatas);
        assertNotNull(internalJmsMessages);
        assertEquals(internalJmsMessages.size(), 1);
        assertEquals(internalJmsMessages.iterator().next(), internalJmsMessage1);
    }

    @Test
    public void testConvertCompositeDataArrayWhenAMessageCannotBeConverted(final @Injectable CompositeData data1,
                                                                           final @Injectable CompositeData data2,
                                              final @Injectable InternalJmsMessage internalJmsMessage1) throws Exception {
        CompositeData[] compositeDatas = new CompositeData[]{data1, data2};

        new Expectations(jmsManagerActiveMQ) {{
            jmsManagerActiveMQ.convertCompositeData(data1);
            result = new RuntimeException("Simulating a message conversion error");

            jmsManagerActiveMQ.convertCompositeData(data2);
            result = internalJmsMessage1;
        }};

        final List<InternalJmsMessage> internalJmsMessages = jmsManagerActiveMQ.convertCompositeData(compositeDatas);
        assertNotNull(internalJmsMessages);
        assertEquals(internalJmsMessages.size(), 1);
        assertEquals(internalJmsMessages.iterator().next(), internalJmsMessage1);
    }

    @Test
    public void testDeleteMessages(final @Injectable QueueViewMBean queueMbean) throws Exception {
        final String myqueue = "myqueue";
        final String[] messageIds = new String[]{"id1", "id2"};

        new Expectations(jmsManagerActiveMQ) {{
            jmsManagerActiveMQ.getQueue(myqueue);
            result = queueMbean;
        }};


        jmsManagerActiveMQ.deleteMessages(myqueue, messageIds);

        new Verifications() {{
            String[] capuredMessageIds = null;
            jmsSelectorUtil.getSelector(capuredMessageIds = withCapture());
            assertArrayEquals(capuredMessageIds, messageIds);

            queueMbean.removeMatchingMessages(anyString);
        }};
    }

    @Test
    public void testMoveMessages(final @Injectable QueueViewMBean queueMbean) throws Exception {
        final String source = "sourceQueue";
        final String destination = "destinationQueue";
        final String[] messageIds = new String[]{"id1", "id2"};

        new Expectations(jmsManagerActiveMQ) {{
            jmsManagerActiveMQ.getQueue(source);
            result = queueMbean;
        }};


        jmsManagerActiveMQ.moveMessages(source, destination, messageIds);

        new Verifications() {{
            String[] capuredMessageIds = null;
            String captureDestination = null;
            jmsSelectorUtil.getSelector(capuredMessageIds = withCapture());
            assertArrayEquals(capuredMessageIds, messageIds);

            queueMbean.moveMatchingMessagesTo(anyString, captureDestination = withCapture());
            assertEquals(captureDestination, destination);
        }};
    }

    @Test
    public void testGetMessage(final @Injectable QueueViewMBean queueMbean,
                               final @Injectable CompositeData compositeData) throws Exception {
        final String myqueue = "myqueue";
        final String messageId = "id1";

        new Expectations(jmsManagerActiveMQ) {{
            jmsManagerActiveMQ.getQueue(myqueue);
            result = queueMbean;

            jmsManagerActiveMQ.convertCompositeData(withAny(compositeData));
        }};


        jmsManagerActiveMQ.getMessage(myqueue, messageId);

        new Verifications() {{
            String capturedMessageId = null;
            queueMbean.getMessage(capturedMessageId = withCapture());
            assertEquals(messageId, capturedMessageId);

            queueMbean.getMessage(anyString);
        }};
    }
}
