package eu.domibus.jms.wildfly;

import eu.domibus.jms.spi.JMSDestinationSPI;
import eu.domibus.jms.spi.JMSManagerSPI;
import eu.domibus.jms.spi.JmsMessageSPI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
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
    protected Map<String, ObjectName> topicMap;

//    @Autowired
    MBeanServerConnection mBeanServerConnection;

    @Override
    public Map<String, JMSDestinationSPI> getDestinations() {
        Map<String, JMSDestinationSPI> destinationMap = new TreeMap<>();


        return destinationMap;
    }

    protected Map<String, ObjectName> getQueueMap() {
        if (queueMap != null) {
            return queueMap;
        }

        queueMap = new HashMap<>();

        return queueMap;
    }

    @Override
    public boolean sendMessage(JmsMessageSPI message, String connectionFactory, String destination, String destinationType) {
        return true;
    }

    @Override
    public boolean deleteMessages(String source, String[] messageIds) {
        return true;
    }

    @Override
    public JmsMessageSPI getMessage(String source, String messageId) {
        return null;
    }


    @Override
    public List<JmsMessageSPI> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause) {
        List<JmsMessageSPI> messages = new ArrayList<>();
        if (source == null) {
            return messages;
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
        return true;
    }
}
