package eu.domibus.jms.activemq;

import eu.domibus.jms.spi.JMSDestinationSPI;
import eu.domibus.jms.spi.JMSManagerSPI;
import eu.domibus.jms.spi.JmsMessageSPI;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.jms.DeliveryMode;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import java.util.*;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
@Component
public class JMSManagerActiveMQ implements JMSManagerSPI {

    private static final Log LOG = LogFactory.getLog(JMSManagerActiveMQ.class);

    private static final String PROPERTY_OBJECT_NAME = "ObjectName";
    private static final String PROPERTY_JNDI_NAME = "Jndi";

    protected Map<String, ObjectName> queueMap;
    protected Map<String, ObjectName> topicMap;

    //TODO
    String activeMQDefaultAdminName = "admin";

    String activeMQDefaultAdminPassword = "123456";

    @Autowired
    MBeanServerConnection mBeanServerConnection;

    @Autowired
    @Qualifier("brokerViewMBean")
    BrokerViewMBean brokerViewMBean;

    @Override
    public Map<String, JMSDestinationSPI> getDestinations() {
        Map<String, JMSDestinationSPI> destinationMap = new TreeMap<>();

        try {
            //build the destinationMap every time in order to get up to date statistics
            for (ObjectName name : getQueueMap().values()) {
                QueueViewMBean queueMbean = MBeanServerInvocationHandler.newProxyInstance(mBeanServerConnection, name, QueueViewMBean.class, true);
                JMSDestinationSPI jmsDestinationSPI = new JMSDestinationSPI();
                jmsDestinationSPI.setName(queueMbean.getName());
                jmsDestinationSPI.setType(JMSDestinationSPI.QUEUE_TYPE);
                jmsDestinationSPI.setNumberOfMessages(queueMbean.getQueueSize());
                jmsDestinationSPI.setProperty(PROPERTY_OBJECT_NAME, name);
                destinationMap.put(queueMbean.getName(), jmsDestinationSPI);
                //TODO check if this is needed
                queueMap.put(queueMbean.getName(), name);
            }
        } catch (Exception e) {
            LOG.error("Error getting destinations", e);
        }

        return destinationMap;
    }

    protected QueueViewMBean getQueue(ObjectName objectName) {
        return MBeanServerInvocationHandler.newProxyInstance(mBeanServerConnection, objectName, QueueViewMBean.class, true);
    }

    protected QueueViewMBean getQueue(String name) {
        ObjectName objectName = getQueueMap().get(name);
        return getQueue(objectName);
    }

    protected Map<String, ObjectName> getQueueMap() {
        if (queueMap != null) {
            return queueMap;
        }

        queueMap = new HashMap<>();
        for (ObjectName name : brokerViewMBean.getQueues()) {
            QueueViewMBean queueMbean = MBeanServerInvocationHandler.newProxyInstance(mBeanServerConnection, name, QueueViewMBean.class, true);
            queueMap.put(queueMbean.getName(), name);
        }

        return queueMap;
    }

    @Override
    public boolean sendMessage(JmsMessageSPI message, String connectionFactory, String destination, String destinationType) {
        QueueViewMBean queue = getQueue(destination);

        Map<String, String> properties = message.getProperties();
        properties.put("JMSType", message.getType());
        properties.put("JMSDeliveryMode", Integer.toString(DeliveryMode.PERSISTENT));
        try {
            queue.sendTextMessage(properties, message.getContent(), activeMQDefaultAdminName, activeMQDefaultAdminPassword);
        } catch (Exception e) {
            LOG.error("Error sending message [" + message + "] to [" + destination + "]");
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteMessages(String source, String[] messageIds) {
        QueueViewMBean queue = getQueue(source);
        try {
            int deleted = queue.removeMatchingMessages(getSelector(messageIds));
            return deleted == messageIds.length;
        } catch (Exception e) {
            LOG.error("Failed to delete messages from source [" + source + "]:" + messageIds, e);
            return false;
        }
    }

    @Override
    public JmsMessageSPI getMessage(String source, String messageId) {
        QueueViewMBean queue = getQueue(source);
        try {
            CompositeData messageMetaData = queue.getMessage(messageId);
            return convert(messageMetaData);
        } catch (OpenDataException e) {
            LOG.error("Failed to get message with id [" + messageId + "]", e);
            return null;
        }
    }


    @Override
    public List<JmsMessageSPI> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause) {
        List<JmsMessageSPI> messages = new ArrayList<>();
        if (source == null) {
            return messages;
        }

        JMSDestinationSPI selectedDestination = getDestinations().get(source);
        if (selectedDestination == null) {
            LOG.debug("Could not find destination for [" + source + "]");
            return messages;
        }
        String destinationType = selectedDestination.getType();
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
            String selector = getSelector(criteria);
            try {
                QueueViewMBean queue = getQueue(source);
                CompositeData[] browse = queue.browse(selector);
                messages = convert(browse);
            } catch (Exception e) {
                LOG.error("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
            }
        }
        return messages;
    }

    protected List<JmsMessageSPI> convert(CompositeData[] browse) {
        if (browse == null) {
            return null;
        }
        List<JmsMessageSPI> result = new ArrayList<>();
        for (CompositeData compositeData : browse) {
            result.add(convert(compositeData));
        }
        return result;
    }


    protected JmsMessageSPI convert(CompositeData data) {
        JmsMessageSPI result = new JmsMessageSPI();
        result.setType((String) data.get("JMSType"));
        result.setTimestamp((Date) data.get("JMSTimestamp"));
        result.setId((String) data.get("JMSMessageID"));
        result.setContent((String) data.get("Text"));
        Map stringProperties = (Map) data.get("StringProperties");

        Map<String, String> properties = new HashMap<>();

        Set<String> allPropertyNames = data.getCompositeType().keySet();
        for (String propertyName : allPropertyNames) {
            if (StringUtils.startsWith(propertyName, "JMS")) {
                Object propertyValue = data.get(propertyName);
                //TODO add other types of properties
                if (propertyValue instanceof String) {
                    properties.put(propertyName, (String) propertyValue);
                }
            }
        }

        Collection<CompositeDataSupport> stringValues = stringProperties.values();
        for (CompositeDataSupport stringValue : stringValues) {
            String key = (String) stringValue.get("key");
            String value = (String) stringValue.get("value");
            properties.put(key, value);
        }
        result.setProperties(properties);
        return result;
    }


    private String getSelector(String messageId) {
        StringBuffer selector = new StringBuffer("JMSMessageID = '").append(messageId).append("'");
        return selector.toString();
    }

    private String getSelector(String[] messageIds) {
        if (messageIds.length == 1) {
            return getSelector(messageIds[0]);
        }
        StringBuffer selector = new StringBuffer("JMSMessageID IN (");
        for (int i = 0; i < messageIds.length; i++) {
            String messageId = messageIds[i];
            if (i > 0) {
                selector.append(", ");
            }
            selector.append("'").append(messageId).append("'");
        }
        selector.append(")");
        return selector.toString();
    }

    public String getSelector(Map<String, Object> criteria) {
        StringBuffer selector = new StringBuffer();
        // JMSType
        String jmsType = (String) criteria.get("JMSType");
        if (!StringUtils.isEmpty(jmsType)) {
            selector.append(selector.length() > 0 ? " and " : "");
            selector.append("JMSType='").append(jmsType).append("'");
        }
        // JMSTimestamp
        Long jmsTimestampFrom = (Long) criteria.get("JMSTimestamp_from");
        if (jmsTimestampFrom != null) {
            selector.append(selector.length() > 0 ? " and " : "");
            selector.append("JMSTimestamp>=").append(jmsTimestampFrom);
        }
        Long jmsTimestampTo = (Long) criteria.get("JMSTimestamp_to");
        if (jmsTimestampTo != null) {
            selector.append(selector.length() > 0 ? " and " : "");
            selector.append("JMSTimestamp<=").append(jmsTimestampTo);
        }
        String selectorClause = (String) criteria.get("selectorClause");
        if (!StringUtils.isEmpty(selectorClause)) {
            selector.append(selector.length() > 0 ? " and " : "");
            selector.append(selectorClause);
        }
        if (selector.length() == 0) {
            selector.append("true");
        }
        return selector.toString().trim();
    }


    @Override
    public boolean moveMessages(String source, String destination, String[] messageIds) {
        QueueViewMBean queue = getQueue(source);
        try {
            int moved = queue.moveMatchingMessagesTo(getSelector(messageIds), destination);
            return moved == messageIds.length;
        } catch (Exception e) {
            LOG.error("Failed to move messages from source [" + source + "] to destination [" + destination + "]:" + messageIds, e);
            return false;
        }
    }
}
