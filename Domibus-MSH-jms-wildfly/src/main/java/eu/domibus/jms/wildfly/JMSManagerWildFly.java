package eu.domibus.jms.wildfly;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.jms.spi.JMSDestinationSPI;
import eu.domibus.jms.spi.JMSManagerSPI;
import eu.domibus.jms.spi.JmsMessageSPI;
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
 * Created by Cosmin Baciu on 17-Aug-16.
 */
@Component
public class JMSManagerWildFly implements JMSManagerSPI {

    private static final Log LOG = LogFactory.getLog(JMSManagerWildFly.class);

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
    public Map<String, JMSDestinationSPI> getDestinations() {
        Map<String, JMSDestinationSPI> destinationMap = new TreeMap<>();

        try {
            Map<String, ObjectName> queueMap = getQueueMap();
            for (ObjectName objectName : queueMap.values()) {
                JMSQueueControl jmsQueueControl = MBeanServerInvocationHandler.newProxyInstance(mBeanServer, objectName, JMSQueueControl.class, false);
                JMSDestinationSPI jmsDestinationSPI = new JMSDestinationSPI();
                jmsDestinationSPI.setName(jmsQueueControl.getName());
                jmsDestinationSPI.setType(JMSDestinationSPI.QUEUE_TYPE);
                jmsDestinationSPI.setNumberOfMessages(jmsQueueControl.getMessageCount());
                jmsDestinationSPI.setProperty(PROPERTY_OBJECT_NAME, objectName);
                jmsDestinationSPI.setProperty(PROPERTY_JNDI_NAME, jmsQueueControl.getAddress());
                jmsDestinationSPI.setInternal(jmsDestinationHelper.isInternal(jmsQueueControl.getAddress()));
                destinationMap.put(jmsQueueControl.getName(), jmsDestinationSPI);
            }
        } catch (Exception e) {
            LOG.error("Failed to build JMS destination map", e);
        }

        return destinationMap;
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
    public boolean sendMessage(JmsMessageSPI message, String destination) {
        JMSDestinationSPI jmsDestinationSPI = getDestinations().get(destination);
        if (jmsDestinationSPI == null) {
            LOG.warn("Destination [" + destination + "] does not exists");
            return false;
        }

        javax.jms.Queue jmsDestination = null;
        try {
            jmsDestination = getQueue(destination);
        } catch (NamingException e) {
            LOG.error("Error performing lookup for [" + destination + "]", e);
            return false;
        }
        sendMessage(message, jmsDestination);
        return true;
    }

    @Override
    public void sendMessage(JmsMessageSPI message, javax.jms.Queue destination) {
        jmsOperations.send(destination, new JmsMessageCreator(message));
    }

    protected javax.jms.Queue getQueue(String queueName) throws NamingException {
        JMSDestinationSPI jmsDestinationSPI = getDestinations().get(queueName);
        String destinationJndi = getJndiName(jmsDestinationSPI);
        LOG.debug("Found JNDI [" + destinationJndi + "] for destination [" + queueName + "]");
        return InitialContext.doLookup(destinationJndi);
    }

    protected String getJndiName(JMSDestinationSPI jmsDestinationSPI) {
        String destinationJndi = jmsDestinationSPI.getProperty(PROPERTY_JNDI_NAME);
        return "java:/" + StringUtils.replace(destinationJndi, ".", "/");
    }

    @Override
    public boolean deleteMessages(String source, String[] messageIds) {
        JMSQueueControl queue = getQueueControl(source);
        try {
            int deleted = queue.removeMessages(jmsSelectorUtil.getSelector(messageIds));
            return deleted == messageIds.length;
        } catch (Exception e) {
            LOG.error("Failed to delete messages from source [" + source + "]:" + messageIds, e);
            return false;
        }
    }

    @Override
    public JmsMessageSPI getMessage(String source, String messageId) {
        String selector = jmsSelectorUtil.getSelector(messageId);

        List<JmsMessageSPI> messages = null;
        try {
            messages = getMessagesFromDestination(source, selector);
        } catch (Exception e) {
            LOG.error("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
            return null;
        }

        if (messages != null && !messages.isEmpty()) {
            return messages.get(0);
        }
        return null;
    }


    @Override
    public List<JmsMessageSPI> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause) {
        List<JmsMessageSPI> messages = new ArrayList<>();
        if (StringUtils.isEmpty(source)) {
            return messages;
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
            LOG.error("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
        }

        return messages;
    }

    private List<JmsMessageSPI> getMessagesFromDestination(String destination, String selector) throws Exception {
//        JMSQueueControl queue = getQueueControl(destination);
        javax.jms.Queue queue = getQueue(destination);
        return jmsOperations.browseSelected(queue, selector, new BrowserCallback<List<JmsMessageSPI>>() {
            @Override
            public List<JmsMessageSPI> doInJms(Session session, QueueBrowser browser) throws JMSException {
                List<JmsMessageSPI> result = new ArrayList<>();
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

    protected JmsMessageSPI convert(TextMessage textMessage) throws JMSException {
        JmsMessageSPI result = new JmsMessageSPI();
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

    protected List<JmsMessageSPI> convert(Map<String, Object>[] maps) {
        if (maps == null) {
            return null;
        }
        List<JmsMessageSPI> result = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            result.add(convert(map));
        }
        return result;
    }

    protected JmsMessageSPI convert(Map<String, Object> map) {
        JmsMessageSPI result = new JmsMessageSPI();

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
    public boolean moveMessages(String source, String destination, String[] messageIds) {
        JMSQueueControl queue = getQueueControl(source);
        try {
            int moved = queue.moveMessages(jmsSelectorUtil.getSelector(messageIds), destination);
            return moved == messageIds.length;
        } catch (Exception e) {
            LOG.error("Failed to move messages from source [" + source + "] to destination [" + destination + "]:" + messageIds, e);
            return false;
        }
    }
}
