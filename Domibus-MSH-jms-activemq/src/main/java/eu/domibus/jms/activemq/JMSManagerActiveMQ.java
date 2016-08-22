package eu.domibus.jms.activemq;

import eu.domibus.jms.spi.JMSDestinationSPI;
import eu.domibus.jms.spi.JMSManagerSPI;
import eu.domibus.jms.spi.JmsMessageSPI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
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

    @Resource(name = "jmsSender")
    private JmsOperations jmsOperations;

    @Override
    public Map<String, JMSDestinationSPI> getDestinations() {
        Map<String, JMSDestinationSPI> destinationMap = new TreeMap<String, JMSDestinationSPI>();

        return destinationMap;
    }

    protected Map<String, ObjectName> getQueueMap(MBeanServerConnection mbsc) throws IOException, AttributeNotFoundException, InstanceNotFoundException, MBeanException,
            ReflectionException {
        if (queueMap != null) {
            return queueMap;
        }

        return queueMap;
    }

    protected Map<String, ObjectName> getTopicMap(MBeanServerConnection mbsc) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException,
            ReflectionException, IOException {
        if (topicMap != null) {
            return topicMap;
        }
        topicMap = new HashMap<String, ObjectName>();

        return topicMap;
    }

    @Override
    public boolean sendMessage(JmsMessageSPI message, String connectionFactory, String destination, String destinationType) {
        JMSDestinationSPI jmsDestinationSPI = getDestinations().get(destination);
        if (jmsDestinationSPI == null) {
            LOG.warn("Destination [" + destination + "] does not exists");
            return false;
        }

        Destination jmsDestination = null;
        try {
            String destinationJndi = jmsDestinationSPI.getProperty(PROPERTY_JNDI_NAME);
            LOG.debug("Found JNDI [" + destinationJndi + "] for destination [" + destination + "]");
            jmsDestination = InitialContext.doLookup(destinationJndi);
        } catch (NamingException e) {
            LOG.error("Error performing lookup for [" + destination + "]");
            return false;
        }
        jmsOperations.send(jmsDestination, new JmsMessageCreator(message));
        return true;
    }

    @Override
    public boolean deleteMessages(String source, String[] messageIds) {
        JMSDestinationSPI selectedDestination = getDestinations().get(source);
        try {
            ObjectName destination = selectedDestination.getProperty(PROPERTY_OBJECT_NAME);
            int deleted = deleteMessages(destination, getSelector(messageIds));
            return deleted == messageIds.length;
        } catch (Exception e) {
            LOG.error("Failed to delete messages", e);
            return false;
        }
    }

    @Override
    public JmsMessageSPI getMessage(String source, String messageId) {
        JMSDestinationSPI selectedDestination = getDestinations().get(source);
        if (selectedDestination != null) {
            String destinationType = selectedDestination.getType();
            if ("Queue".equals(destinationType)) {
                try {
                    ObjectName destination = selectedDestination.getProperty(PROPERTY_OBJECT_NAME);
                    return getMessageFromDestination(destination, messageId);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    private JmsMessageSPI getMessageFromDestination(ObjectName destination, String messageId) throws Exception {
        List<JmsMessageSPI> messages = getMessagesFromDestination(destination, getSelector(messageId));
        if (!messages.isEmpty()) {
            return messages.get(0);
        }
        return null;
    }

    private List<JmsMessageSPI> getMessagesFromDestination(ObjectName destination, String selector) throws Exception {
        List<JmsMessageSPI> messages = new ArrayList<JmsMessageSPI>();
        MBeanServerConnection mbsc = null;//jmxHelper.getDomainRuntimeMBeanServerConnection();
        if (selector == null) {
            selector = "true";
        }
        Integer timeout = new Integer(0);

        return messages;
    }

    @Override
    public List<JmsMessageSPI> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause) {
        List<JmsMessageSPI> messages = new ArrayList<>();
        if (source == null) {
            return messages;
        }

        JMSDestinationSPI selectedDestination = getDestinations().get(source);
        if (selectedDestination != null) {
            String destinationType = selectedDestination.getType();
            if ("Queue".equals(destinationType)) {
                try {
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
                    ObjectName destination = selectedDestination.getProperty(PROPERTY_OBJECT_NAME);
                    messages = getMessagesFromDestination(destination, selector);
                } catch (Exception e) {
                    LOG.error("Error getting messages", e);
                }
            }
        }
        return messages;
    }

    private int deleteMessages(ObjectName destination, String selector) throws Exception {
        MBeanServerConnection mbsc = null;//jmxHelper.getDomainRuntimeMBeanServerConnection();
        Integer deleted = (Integer) mbsc.invoke(destination, "deleteMessages", new Object[]{selector}, new String[]{String.class.getName()});
        return deleted;
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
        JMSDestinationSPI from = getDestinations().get(source);
        JMSDestinationSPI to = getDestinations().get(destination);
        try {
            ObjectName fromDestination = from.getProperty(PROPERTY_OBJECT_NAME);
            ObjectName toDestination = to.getProperty(PROPERTY_OBJECT_NAME);
            int moved = moveMessages(fromDestination, toDestination, getSelector(messageIds));
            return moved == messageIds.length;
        } catch (Exception e) {
            LOG.error("Failed to move messages", e);
            return false;
        }
    }

    private int moveMessages(ObjectName from, ObjectName to, String selector) throws Exception {
        MBeanServerConnection mbsc = null;//jmxHelper.getDomainRuntimeMBeanServerConnection();
        CompositeData toDestinationInfo = (CompositeData) mbsc.getAttribute(to, "DestinationInfo");
        Integer moved = (Integer) mbsc.invoke(from, "moveMessages", new Object[]{selector, toDestinationInfo}, new String[]{String.class.getName(),
                CompositeData.class.getName()});
        return moved;
    }
}
