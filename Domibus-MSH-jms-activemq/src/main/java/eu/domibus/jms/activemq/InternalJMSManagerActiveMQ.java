package eu.domibus.jms.activemq;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.jms.spi.InternalJMSDestination;
import eu.domibus.jms.spi.InternalJMSException;
import eu.domibus.jms.spi.InternalJMSManager;
import eu.domibus.jms.spi.InternalJmsMessage;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import eu.domibus.jms.spi.helper.JmsMessageCreator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(InternalJMSManagerActiveMQ.class);

    private static final String PROPERTY_OBJECT_NAME = "ObjectName";

    protected Map<String, ObjectName> queueMap;

    @Autowired
    MBeanServerConnection mBeanServerConnection;

    @Autowired
    @Qualifier("brokerViewMBean")
    BrokerViewMBean brokerViewMBean;

    @Autowired
    BrokerService brokerService;

    @Autowired
    JMSDestinationHelper jmsDestinationHelper;

    @Resource(name = "jmsSender")
    private JmsOperations jmsOperations;

    @Autowired
    JMSSelectorUtil jmsSelectorUtil;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Override
    public Map<String, InternalJMSDestination> findDestinationsGroupedByFQName() {

        Map<String, InternalJMSDestination> destinationMap = new TreeMap<>();

        try {
            for (ObjectName name : getQueueMap().values()) {
                QueueViewMBean queueMbean = getQueueViewMBean(name);
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
        /* in multi-tenancy mode we show the number of messages only to super admin */
        internalJmsDestination.setNumberOfMessages(domibusConfigurationService.isMultiTenantAware() && !authUtils.isSuperAdmin() ? NB_MESSAGES_ADMIN : queueMbean.getQueueSize());
        internalJmsDestination.setProperty(PROPERTY_OBJECT_NAME, name);
        return internalJmsDestination;
    }

    protected QueueViewMBean getQueueViewMBean(ObjectName objectName) {
        return MBeanServerInvocationHandler.newProxyInstance(mBeanServerConnection, objectName, QueueViewMBean.class, true);
    }

    protected QueueViewMBean getQueueViewMBean(String name) {
        ObjectName objectName = getQueueMap().get(name);
        return getQueueViewMBean(objectName);
    }

    protected Map<String, ObjectName> getQueueMap() {
        if (queueMap != null) {
            return queueMap;
        }

        queueMap = new HashMap<>();
        for (ObjectName name : brokerViewMBean.getQueues()) {
            QueueViewMBean queueMbean = getQueueViewMBean(name);
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
    public void sendMessage(InternalJmsMessage message, Destination destination) {
        jmsOperations.send(destination, new JmsMessageCreator(message));
    }

    @Override
    public void deleteMessages(String source, String[] messageIds) {
        try {
            QueueViewMBean queue = getQueueViewMBean(source);
            queue.removeMatchingMessages(jmsSelectorUtil.getSelector(messageIds));
        } catch (Exception e) {
            throw new InternalJMSException("Failed to delete messages from source [" + source + "]:" + messageIds, e);
        }
    }

    @Override
    public InternalJmsMessage getMessage(String source, String messageId) {
        try {
            QueueViewMBean queue = getQueueViewMBean(source);
            CompositeData messageMetaData = queue.getMessage(messageId);
            return convertCompositeData(messageMetaData);
        } catch (OpenDataException e) {
            throw new InternalJMSException("Failed to get message with id [" + messageId + "]", e);
        }
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

        return getInternalJmsMessages(source, jmsType, fromDate, toDate, selectorClause, destination.getType());

    }

    private List<InternalJmsMessage> getInternalJmsMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause, String destinationType) {
        List<InternalJmsMessage> internalJmsMessages = new ArrayList<>();
        if (QUEUE.equals(destinationType)) {
            Map<String, Object> criteria = new HashMap<>();
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
                QueueViewMBean queue = getQueueViewMBean(source);
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
            return Collections.emptyList();
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
            QueueViewMBean queue = getQueueViewMBean(source);
            queue.moveMatchingMessagesTo(jmsSelectorUtil.getSelector(messageIds), destination);
        } catch (Exception e) {
            throw new InternalJMSException("Failed to move messages from source [" + source + "] to destination [" + destination + "]:" + messageIds, e);
        }
    }

    @Override
    public InternalJmsMessage consumeMessage(String source, String customMessageId) {

        String selector = "MESSAGE_ID='" + customMessageId + "' AND NOTIFICATION_TYPE ='MESSAGE_RECEIVED'";

        InternalJmsMessage intJmsMsg = null;
        try {
            QueueViewMBean queueViewMBean = getQueueViewMBean(source);
            List<InternalJmsMessage> messages = getMessagesFromDestination(source, selector);
            if (!messages.isEmpty()) {
                intJmsMsg = messages.get(0);
                // Deletes it
                queueViewMBean.removeMatchingMessages(selector);
            }
        } catch (Exception ex) {
            throw new InternalJMSException("Failed to consume message [" + customMessageId + "] from source [" + source + "]", ex);
        }
        return intJmsMsg;
    }

    //TODO: Duplicate code that will be refactored in the scope of a task in 4.0
    protected List<InternalJmsMessage> getMessagesFromDestination(String destination, String selector) throws Exception {
        Queue queue;

        try {
            queue = getQueue(destination);
        } catch (Exception ex) {
            throw new JMSActiveMQException(ex);
        }
        return jmsOperations.browseSelected(queue, selector, (session, browser) -> {
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
            properties.put(name, objectProperty);
        }
        result.setProperties(properties);
        return result;
    }

    protected Queue getQueue(String queueName) throws JMSActiveMQException {
        final ActiveMQDestination[] destinations;
        try {
            Broker broker = brokerService.getBroker();
            destinations = broker.getDestinations();
        } catch (Exception ex) {
            throw new JMSActiveMQException(ex);
        }

        for(ActiveMQDestination destination : destinations) {
            if(queueName.equals(destination.getPhysicalName())) {
                return (Queue) destination;
            }
        }
        return null;
    }
}
