package eu.domibus.jms.activemq;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.jms.spi.JMSDestinationSPI;
import eu.domibus.jms.spi.JMSManagerSPI;
import eu.domibus.jms.spi.JmsMessageSPI;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import eu.domibus.jms.spi.helper.JmsMessageCreator;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    protected Map<String, ObjectName> queueMap;

    @Autowired
    MBeanServerConnection mBeanServerConnection;

    @Autowired
    @Qualifier("brokerViewMBean")
    BrokerViewMBean brokerViewMBean;

    @Autowired
    JMSDestinationHelper jmsDestinationHelper;

    @Resource(name = "jmsSender")
    private JmsOperations jmsOperations;

    @Autowired
    JMSSelectorUtil jmsSelectorUtil;

    @Override
    public Map<String, JMSDestinationSPI> getDestinations() {
        Map<String, JMSDestinationSPI> destinationMap = new TreeMap<>();

        try {
            //build the destinationMap every time in order to get up to date statistics
            for (ObjectName name : getQueueMap().values()) {
                QueueViewMBean queueMbean = getQueue(name);
                JMSDestinationSPI jmsDestinationSPI = createJmsDestinationSPI(name, queueMbean);
                destinationMap.put(queueMbean.getName(), jmsDestinationSPI);
            }
        } catch (Exception e) {
            LOG.error("Error getting destinations", e);
        }

        return destinationMap;
    }

    protected JMSDestinationSPI createJmsDestinationSPI(ObjectName name, QueueViewMBean queueMbean) {
        JMSDestinationSPI jmsDestinationSPI = new JMSDestinationSPI();
        jmsDestinationSPI.setName(queueMbean.getName());
        jmsDestinationSPI.setInternal(jmsDestinationHelper.isInternal(queueMbean.getName()));
        jmsDestinationSPI.setType(JMSDestinationSPI.QUEUE_TYPE);
        jmsDestinationSPI.setNumberOfMessages(queueMbean.getQueueSize());
        jmsDestinationSPI.setProperty(PROPERTY_OBJECT_NAME, name);
        return jmsDestinationSPI;
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
            QueueViewMBean queueMbean = getQueue(name);
            queueMap.put(queueMbean.getName(), name);
        }

        return queueMap;
    }

    @Override
    public boolean sendMessage(JmsMessageSPI message, String destination) {
        ActiveMQQueue activeMQQueue = new ActiveMQQueue(destination);
        sendMessage(message, activeMQQueue);
        return true;
    }

    @Override
    public void sendMessage(JmsMessageSPI message, javax.jms.Queue destination) {
        jmsOperations.send(destination, new JmsMessageCreator(message));
    }

    @Override
    public boolean deleteMessages(String source, String[] messageIds) {
        QueueViewMBean queue = getQueue(source);
        try {
            int deleted = queue.removeMatchingMessages(jmsSelectorUtil.getSelector(messageIds));
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
            return convertCompositeData(messageMetaData);
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
            String selector = jmsSelectorUtil.getSelector(criteria);
            try {
                QueueViewMBean queue = getQueue(source);
                CompositeData[] browse = queue.browse(selector);
                messages = convertCompositeData(browse);
            } catch (Exception e) {
                LOG.error("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
            }
        }
        return messages;
    }

    protected List<JmsMessageSPI> convertCompositeData(CompositeData[] browse) {
        if (browse == null) {
            return null;
        }
        List<JmsMessageSPI> result = new ArrayList<>();
        for (CompositeData compositeData : browse) {
            try {
                final JmsMessageSPI jmsMessageSPI = convertCompositeData(compositeData);
                result.add(jmsMessageSPI);
            } catch (Exception e) {
                LOG.error("Error converting message [" + compositeData + "]", e);
            }
        }
        return result;
    }


    protected JmsMessageSPI convertCompositeData(CompositeData data) {
        JmsMessageSPI result = new JmsMessageSPI();
        result.setType((String) data.get("JMSType"));
        result.setTimestamp((Date) data.get("JMSTimestamp"));
        result.setId((String) data.get("JMSMessageID"));
        if(data.containsKey("Text")) {
            result.setContent((String) data.get("Text"));
        }

        Map stringProperties = (Map) data.get("StringProperties");

        Map<String, Object> properties = new HashMap<>();

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
        for (CompositeDataSupport compositeDataSupport : stringValues) {
            String key = (String) compositeDataSupport.get("key");
            String value = (String) compositeDataSupport.get("value");
            properties.put(key, value);
        }
        result.setProperties(properties);
        return result;
    }

    @Override
    public boolean moveMessages(String source, String destination, String[] messageIds) {
        QueueViewMBean queue = getQueue(source);
        try {
            int moved = queue.moveMatchingMessagesTo(jmsSelectorUtil.getSelector(messageIds), destination);
            return moved == messageIds.length;
        } catch (Exception e) {
            LOG.error("Failed to move messages from source [" + source + "] to destination [" + destination + "]:" + messageIds, e);
            return false;
        }
    }
}
