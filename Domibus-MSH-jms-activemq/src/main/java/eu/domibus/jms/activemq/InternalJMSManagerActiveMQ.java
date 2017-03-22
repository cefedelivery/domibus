package eu.domibus.jms.activemq;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.jms.spi.InternalJMSDestination;
import eu.domibus.jms.spi.InternalJMSException;
import eu.domibus.jms.spi.InternalJMSManager;
import eu.domibus.jms.spi.InternalJmsMessage;
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
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class InternalJMSManagerActiveMQ implements InternalJMSManager {

    private static final Log LOG = LogFactory.getLog(InternalJMSManagerActiveMQ.class);

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
    public Map<String, InternalJMSDestination> findDestinationsGroupedByFQName() {

        Map<String, InternalJMSDestination> destinationMap = new TreeMap<>();

        try {
            for (ObjectName name : getQueueMap().values()) {
                QueueViewMBean queueMbean = getQueue(name);
                InternalJMSDestination internalJmsDestination = createInternalJmsDestination(name, queueMbean);
                destinationMap.put(queueMbean.getName(), internalJmsDestination);
            }
            return destinationMap;
        } catch (Exception e) {
            throw new InternalJMSException("Error getting destinations", e);
        }
    }


    protected InternalJMSDestination createInternalJmsDestination(ObjectName name, QueueViewMBean queueMbean) {
        InternalJMSDestination internalJmsDestination = new InternalJMSDestination();
        internalJmsDestination.setName(queueMbean.getName());
        internalJmsDestination.setInternal(jmsDestinationHelper.isInternal(queueMbean.getName()));
        internalJmsDestination.setType(InternalJMSDestination.QUEUE_TYPE);
        internalJmsDestination.setNumberOfMessages(queueMbean.getQueueSize());
        internalJmsDestination.setProperty(PROPERTY_OBJECT_NAME, name);
        return internalJmsDestination;
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
    public void sendMessage(InternalJmsMessage message, String destination) {
        ActiveMQQueue activeMQQueue = new ActiveMQQueue(destination);
        sendMessage(message, activeMQQueue);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, javax.jms.Destination destination) {
        jmsOperations.send(destination, new JmsMessageCreator(message));
    }

    @Override
    public void deleteMessages(String source, String[] messageIds) {
        try {
            QueueViewMBean queue = getQueue(source);
            queue.removeMatchingMessages(jmsSelectorUtil.getSelector(messageIds));
        } catch (Exception e) {
            throw new InternalJMSException("Failed to delete messages from source [" + source + "]:" + messageIds, e);
        }
    }

    @Override
    public InternalJmsMessage getMessage(String source, String messageId) {
        try {
            QueueViewMBean queue = getQueue(source);
            CompositeData messageMetaData = queue.getMessage(messageId);
            return convertCompositeData(messageMetaData);
        } catch (OpenDataException e) {
            throw new InternalJMSException("Failed to get message with id [" + messageId + "]", e);
        }
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
        InternalJMSDestination destination = findDestinationsGroupedByFQName().get(source);
        if (destination == null) {
            throw new InternalJMSException("Could not find destination for [" + source + "]");
        }
        List<InternalJmsMessage> internalJmsMessages = new ArrayList<>();
        String destinationType = destination.getType();
        if (QUEUE.equals(destinationType)) {
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
            if (StringUtils.isEmpty(selector)) {
                selector = "true";
            }
            try {
                QueueViewMBean queue = getQueue(source);
                CompositeData[] browse = queue.browse(selector);
                internalJmsMessages.addAll(convertCompositeData(browse));
            } catch (Exception e) {
                throw new InternalJMSException("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
            }
        } else {
            throw new InternalJMSException("Unrecognized destination type [" + destinationType + "]");
        }
        return internalJmsMessages;
    }

    protected List<InternalJmsMessage> convertCompositeData(CompositeData[] browse) {
        if (browse == null) {
            return null;
        }
        List<InternalJmsMessage> result = new ArrayList<>();
        for (CompositeData compositeData : browse) {
            try {
                InternalJmsMessage internalJmsMessage = convertCompositeData(compositeData);
                result.add(internalJmsMessage);
            } catch (Exception e) {
                LOG.error("Error converting message [" + compositeData + "]", e);
            }
        }
        return result;
    }

    protected <T> T getCompositeValue(CompositeData data, String name) {
        if (data.containsKey(name)) {
            return (T) data.get(name);
        }
        return null;
    }


    protected InternalJmsMessage convertCompositeData(CompositeData data) {
        InternalJmsMessage result = new InternalJmsMessage();
        String jmsType = getCompositeValue(data, "JMSType");
        result.setType(jmsType);
        Date jmsTimestamp = getCompositeValue(data, "JMSTimestamp");
        result.setTimestamp(jmsTimestamp);
        String jmsMessageId = getCompositeValue(data, "JMSMessageID");
        result.setId(jmsMessageId);
        String textValue = getCompositeValue(data, "Text");
        result.setContent(textValue);

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
    public void moveMessages(String source, String destination, String[] messageIds) {
        try {
            QueueViewMBean queue = getQueue(source);
            queue.moveMatchingMessagesTo(jmsSelectorUtil.getSelector(messageIds), destination);
        } catch (Exception e) {
            throw new InternalJMSException("Failed to move messages from source [" + source + "] to destination [" + destination + "]:" + messageIds, e);
        }
    }

    @Override
    public InternalJmsMessage consumeMessage(String source, String customMessageId) {

        String selector = "MESSAGE_ID='" + customMessageId + "'";

        InternalJmsMessage intJmsMsg = null;
        try {
            QueueViewMBean queue = getQueue(source);
            CompositeData[] browse = queue.browse(selector);
            List<InternalJmsMessage> messages = convertCompositeData(browse);
            if (!messages.isEmpty()) {
                intJmsMsg = messages.get(0);
                // Deletes it
                queue.removeMatchingMessages(selector);
            }
        } catch (Exception ex) {
            throw new InternalJMSException("Failed to consume message [" + customMessageId + "] from source [" + source + "]", ex);
        }
        return intJmsMsg;
    }
}
