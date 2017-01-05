package eu.domibus.jms.wildfly;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.jms.spi.InternalJMSDestination;
import eu.domibus.jms.spi.InternalJMSException;
import eu.domibus.jms.spi.InternalJMSManager;
import eu.domibus.jms.spi.InternalJmsMessage;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import eu.domibus.jms.spi.helper.JmsMessageCreator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.api.jms.management.JMSQueueControl;
import org.hornetq.api.jms.management.JMSServerControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
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

    private static final Log LOG = LogFactory.getLog(InternalJMSManagerWildFly.class);

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
    public Map<String, InternalJMSDestination> getDestinations() {
        Map<String, InternalJMSDestination> destinationMap = new TreeMap<>();

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
                destinationMap.put(jmsQueueControl.getName(), internalJmsDestination);
            }
            return destinationMap;
        } catch (Exception e) {
            throw new InternalJMSException("Failed to build JMS destination map", e);
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

    @Override
    public void sendMessage(InternalJmsMessage message, String destination) {
        InternalJMSDestination internalJmsDestination = getDestinations().get(destination);
        if (internalJmsDestination == null) {
            throw new InternalJMSException("Destination [" + destination + "] does not exists");
        }

        javax.jms.Queue jmsDestination = null;
        try {
            jmsDestination = getQueue(destination);
        } catch (NamingException e) {
            throw new InternalJMSException("Error performing lookup for [" + destination + "]", e);
        }
        sendMessage(message, jmsDestination);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, javax.jms.Queue destination) {
        jmsOperations.send(destination, new JmsMessageCreator(message));
    }

    protected javax.jms.Queue getQueue(String queueName) throws NamingException {
        InternalJMSDestination internalJmsDestination = getDestinations().get(queueName);
        String destinationJndi = getJndiName(internalJmsDestination);
        LOG.debug("Found JNDI [" + destinationJndi + "] for destination [" + queueName + "]");
        return InitialContext.doLookup(destinationJndi);
    }

    protected String getJndiName(InternalJMSDestination internalJmsDestination) {
        String destinationJndi = internalJmsDestination.getProperty(PROPERTY_JNDI_NAME);
        return "java:/" + StringUtils.replace(destinationJndi, ".", "/");
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
    public List<InternalJmsMessage> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause) {
        List<InternalJmsMessage> messages = new ArrayList<>();
        if (StringUtils.isEmpty(source)) {
            throw new InternalJMSException("Source has not been specified");
        }
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
            return getMessagesFromDestination(source, selector);
        } catch (Exception e) {
            throw new InternalJMSException("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
        }
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
