package eu.domibus.jms.wildfly;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.jms.spi.InternalJMSDestination;
import eu.domibus.jms.spi.InternalJMSException;
import eu.domibus.jms.spi.InternalJMSManager;
import eu.domibus.jms.spi.InternalJmsMessage;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import eu.domibus.jms.spi.helper.JmsMessageCreator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.hornetq.api.jms.management.JMSQueueControl;
import org.hornetq.api.jms.management.JMSServerControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.*;
import javax.jms.Queue;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class InternalJMSManagerWildFly implements InternalJMSManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(InternalJMSManagerWildFly.class);

    private static final String PROPERTY_OBJECT_NAME = "ObjectName";
    private static final String PROPERTY_JNDI_NAME = "Jndi";

    protected Map<String, ObjectName> queueMap;

    @Autowired
    MBeanServer mBeanServer;

    @Autowired
    @Qualifier("jmsServerControl")
    JMSServerControl jmsServerControl;

    @Resource(name = "jmsSender")
    private JmsOperations jmsOperations;

    @Autowired
    JMSDestinationHelper jmsDestinationHelper;

    @Autowired
    JMSSelectorUtil jmsSelectorUtil;

    @Override
    public Map<String, List<InternalJMSDestination>> getDestinations() {
        Map<String, List<InternalJMSDestination>> destinationMap = new TreeMap<>();

        try {
            Map<String, ObjectName> queueMap = getQueueMap();
            for (ObjectName objectName : queueMap.values()) {
                JMSQueueControl jmsQueueControl = MBeanServerInvocationHandler.newProxyInstance(mBeanServer, objectName, JMSQueueControl.class, false);
                InternalJMSDestination internalJmsDestination = new InternalJMSDestination();
                internalJmsDestination.setName(jmsQueueControl.getName());
                internalJmsDestination.setType(InternalJMSDestination.QUEUE_TYPE);
                internalJmsDestination.setNumberOfMessages(jmsQueueControl.getMessageCount());
                internalJmsDestination.setProperty(PROPERTY_OBJECT_NAME, objectName);
                internalJmsDestination.setProperty(PROPERTY_JNDI_NAME, jmsQueueControl.getAddress());
                internalJmsDestination.setInternal(jmsDestinationHelper.isInternal(jmsQueueControl.getAddress()));
                addDestination(destinationMap, internalJmsDestination);
            }
            return destinationMap;
        } catch (Exception e) {
            throw new InternalJMSException("Failed to build JMS destination map", e);
        }
    }

    private void addDestination(Map<String, List<InternalJMSDestination>> destinationMap, InternalJMSDestination destination) {
        if (destinationMap.containsKey(destination.getName())) {
            List<InternalJMSDestination> destinations = destinationMap.get(destination.getName());
            destinations.add(destination);
        } else {
            List<InternalJMSDestination> destinations = new ArrayList<>();
            destinations.add(destination);
            destinationMap.put(destination.getName(), destinations);
        }
    }

    protected JMSQueueControl getQueueControl(ObjectName objectName) {
        return MBeanServerInvocationHandler.newProxyInstance(mBeanServer, objectName, JMSQueueControl.class, false);
    }

    protected JMSQueueControl getQueueControl(String name) {
        ObjectName objectName = getQueueMap().get(name);
        return getQueueControl(objectName);
    }

    protected Map<String, ObjectName> getQueueMap() {
        if (queueMap != null) {
            return queueMap;
        }

        queueMap = new HashMap<>();
        String[] queueNames = jmsServerControl.getQueueNames();
        for (String queueName : queueNames) {
            //TODO externalize this
            String mbeanObjectName = "org.hornetq:module=JMS,type=Queue,name=\"" + queueName + "\"";

            try {
                ObjectName objectName = ObjectName.getInstance(mbeanObjectName);
                queueMap.put(queueName, objectName);
            } catch (MalformedObjectNameException e) {
                LOG.error("Error getting queue [" + queueName + "] using mbeanName [" + mbeanObjectName + "]", e);
            }
        }
        return queueMap;
    }

    protected Queue getQueue(String queueName) throws NamingException {
        return (Queue) lookupDestination(queueName);
    }

    protected Topic getTopic(String topicName) throws NamingException {
        return (Topic) lookupDestination(topicName);
    }

    protected String getJndiName(InternalJMSDestination internalJmsDestination) {
        String destinationJndi = internalJmsDestination.getProperty(PROPERTY_JNDI_NAME);
        return "java:/" + StringUtils.replace(destinationJndi, ".", "/");
    }

    // TODO to be put in helper or super class
    protected javax.jms.Destination lookupDestination(String destName) throws NamingException {
        // It is enough to get the first destination object also in case of clustered destinations because then a JNDI look up is performed.
        InternalJMSDestination internalJmsDestination = getDestinations().get(destName).get(0);
        if (internalJmsDestination == null) {
            throw new InternalJMSException("Destination [" + destName + "] does not exists");
        }
        String destinationJndi = getJndiName(internalJmsDestination);
        LOG.debug("Found JNDI [" + destinationJndi + "] for destination [" + destName + "]");
        return InitialContext.doLookup(destinationJndi);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, String destName) {
        try {
            jmsOperations.send(lookupDestination(destName), new JmsMessageCreator(message));
        } catch (NamingException e) {
            throw new InternalJMSException("Error performing lookup for [" + destName + "]", e);
        }
    }

    @Override
    public void sendMessage(InternalJmsMessage message, Destination destination) {
        jmsOperations.send(destination, new JmsMessageCreator(message));
    }

    @Override
    public void deleteMessages(String source, String[] messageIds) {
        JMSQueueControl queue = getQueueControl(source);
        try {
            queue.removeMessages(jmsSelectorUtil.getSelector(messageIds));
        } catch (Exception e) {
            throw new InternalJMSException("Failed to delete messages from source [" + source + "]:" + messageIds, e);
        }
    }

    @Override
    public InternalJmsMessage getMessage(String source, String messageId) {
        String selector = jmsSelectorUtil.getSelector(messageId);

        try {
            List<InternalJmsMessage> messages = getMessagesFromDestination(source, selector);
            if (messages != null && !messages.isEmpty()) {
                return messages.get(0);
            }
        } catch (Exception e) {
            throw new InternalJMSException("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
        }
        return null;
    }

    @Override
    public List<InternalJmsMessage> browseMessages(String source) {
        return browseMessages(source, null, null, null, null);
    }

    @Override
    public List<InternalJmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause) {
        if (StringUtils.isEmpty(source)) {
            throw new InternalJMSException("Source has not been specified");
        }
        List<InternalJMSDestination> destinations = getDestinations().get(source);
        if (destinations == null || destinations.isEmpty()) {
            throw new InternalJMSException("Could not find destination for [" + source + "]");
        }
        List<InternalJmsMessage> internalJmsMessages = new ArrayList<>();
        for (InternalJMSDestination destination : destinations) {
            String destinationType = destination.getType();
            if ("Queue".equals(destinationType)) {
                Map<String, Object> criteria = new HashMap<String, Object>();
                if (jmsType != null) {
                    criteria.put("JMSType", jmsType);
                }
                if (fromDate != null) {
                    criteria.put("JMSTimestamp_from", fromDate.getTime());
                }
                if (toDate != null) {
                    criteria.put("JMSTimestamp_to", toDate.getTime());
                }
                if (selectorClause != null) {
                    criteria.put("selectorClause", selectorClause);
                }
                String selector = jmsSelectorUtil.getSelector(criteria);
                try {
                    internalJmsMessages.addAll(getMessagesFromDestination(source, selector));
                } catch (Exception e) {
                    throw new InternalJMSException("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
                }
            }
            throw new InternalJMSException("Unrecognized destination type [" + destinationType + "]");
        }
        return internalJmsMessages;
    }

    private List<InternalJmsMessage> getMessagesFromDestination(String destination, String selector) throws NamingException {
        javax.jms.Queue queue = getQueue(destination);
        return jmsOperations.browseSelected(queue, selector, new BrowserCallback<List<InternalJmsMessage>>() {
            @Override
            public List<InternalJmsMessage> doInJms(Session session, QueueBrowser browser) throws JMSException {
                List<InternalJmsMessage> result = new ArrayList<>();
                Enumeration enumeration = browser.getEnumeration();
                while (enumeration.hasMoreElements()) {
                    TextMessage textMessage = null;
                    try {
                        textMessage = (TextMessage) enumeration.nextElement();
                        result.add(convert(textMessage));
                    } catch (Exception e) {
                        LOG.error("Error converting message [" + textMessage + "]", e);
                    }

                }
                return result;
            }
        });
    }

    protected InternalJmsMessage convert(TextMessage textMessage) throws JMSException {
        InternalJmsMessage result = new InternalJmsMessage();
        result.setContent(textMessage.getText());
        result.setId(textMessage.getJMSMessageID());
        result.setTimestamp(new Date(textMessage.getJMSTimestamp()));
        result.setType(textMessage.getJMSType());
        Enumeration propertyNames = textMessage.getPropertyNames();

        Map<String, Object> properties = new HashMap<>();
        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            Object objectProperty = textMessage.getObjectProperty(name);
//            if (objectProperty instanceof String) {
            properties.put(name, objectProperty);
//            }
        }
        result.setProperties(properties);
        return result;
    }

    protected List<InternalJmsMessage> convert(Map<String, Object>[] maps) {
        if (maps == null) {
            return null;
        }
        List<InternalJmsMessage> result = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            result.add(convert(map));
        }
        return result;
    }

    protected InternalJmsMessage convert(Map<String, Object> map) {
        InternalJmsMessage result = new InternalJmsMessage();

        result.setType((String) map.get("JMSType"));
        Long jmsTimestamp = (Long) map.get("JMSTimestamp");
        if (jmsTimestamp != null) {
            result.setTimestamp(new Date(jmsTimestamp));
        }

        result.setId((String) map.get("JMSMessageID"));

        Map<String, Object> properties = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object propertyValue = entry.getValue();

//            if (propertyValue instanceof String) {
            properties.put(entry.getKey(), propertyValue);
//            }
        }
        result.setProperties(properties);
        return result;
    }

    @Override
    public void moveMessages(String source, String destination, String[] messageIds) {
        try {
            JMSQueueControl queue = getQueueControl(source);
            queue.moveMessages(jmsSelectorUtil.getSelector(messageIds), destination);
        } catch (Exception e) {
            throw new InternalJMSException("Failed to move messages from source [" + source + "] to destination [" + destination + "]:" + messageIds, e);
        }
    }
}
