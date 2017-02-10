package eu.domibus.jms.weblogic;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import weblogic.messaging.runtime.MessageInfo;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.Topic;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class InternalJMSManagerWeblogic implements InternalJMSManager {

    private static final Log LOG = LogFactory.getLog(InternalJMSManagerWeblogic.class);

    private static final String PROPERTY_OBJECT_NAME = "ObjectName";
    private static final String PROPERTY_JNDI_NAME = "Jndi";

    protected Map<String, ObjectName> queueMap;

    @Autowired
    JMXHelper jmxHelper;

    @Autowired
    JMXTemplate jmxTemplate;

    @Resource(name = "jmsSender")
    private JmsOperations jmsOperations;

    @Autowired
    JMSDestinationHelper jmsDestinationHelper;

    @Autowired
    JMSSelectorUtil jmsSelectorUtil;

    @Override
    public Map<String, List<InternalJMSDestination>> getDestinations() {
        return jmxTemplate.query(
                new JMXOperation() {
                    @Override
                    public Map<String, List<InternalJMSDestination>> execute(MBeanServerConnection mbsc) {
                        return doGetDestinations(mbsc);
                    }
                }
        );
    }

    protected Map<String, List<InternalJMSDestination>> doGetDestinations(MBeanServerConnection mbsc) {
        Map<String, List<InternalJMSDestination>> destinationMap = new TreeMap<>();
        try {
            ObjectName drs = jmxHelper.getDomainRuntimeService();
            ObjectName[] servers = (ObjectName[]) mbsc.getAttribute(drs, "ServerRuntimes");
            for (ObjectName server : servers) {
                LOG.debug("Server " + server);
                ObjectName jmsRuntime = (ObjectName) mbsc.getAttribute(server, "JMSRuntime");
                ObjectName[] jmsServers = (ObjectName[]) mbsc.getAttribute(jmsRuntime, "JMSServers");
                for (ObjectName jmsServer : jmsServers) {
                    LOG.debug("JMS Server " + jmsServer);
                    ObjectName[] jmsDestinations = (ObjectName[]) mbsc.getAttribute(jmsServer, "Destinations");
                    for (ObjectName jmsDestination : jmsDestinations) {
                        LOG.debug("JMS Destination " + jmsDestination);
                        InternalJMSDestination destination = new InternalJMSDestination();
                        String destinationFQName = (String) mbsc.getAttribute(jmsDestination, "Name");
                        String destinationName = getShortDestName(destinationFQName);
                        destination.setName(destinationName);

                        ObjectName configQueue = getQueueMap(mbsc).get(destinationName);
                        if (configQueue != null) {
                            destination.setType("Queue");
                            destination.setProperty(PROPERTY_OBJECT_NAME, jmsDestination);
                            String configQueueJndiName = (String) mbsc.getAttribute(configQueue, "JNDIName");
                            destination.setProperty(PROPERTY_JNDI_NAME, configQueueJndiName);
                            destination.setInternal(jmsDestinationHelper.isInternal(configQueueJndiName));
                        }

                        destination.setNumberOfMessages(getMessagesTotalCount(mbsc, jmsDestination));
                        addDestination(destinationMap, destination);
                    }
                }
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

    protected Long getMessagesTotalCount(MBeanServerConnection mbsc, ObjectName jmsDestination) throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        Long result = 0L;

        Long messagesCurrentCount = (Long) mbsc.getAttribute(jmsDestination, "MessagesCurrentCount");
        if (messagesCurrentCount != null) {
            result += messagesCurrentCount;
        }
        Long messagesPendingCount = (Long) mbsc.getAttribute(jmsDestination, "MessagesPendingCount");
        if (messagesPendingCount != null) {
            result += messagesPendingCount;
        }

        return result;
    }

    protected String getShortDestName(String destinationName) {
        String result = destinationName;
        if (result.contains("!")) {
            result = result.substring(result.lastIndexOf("!") + 1);
        }
        if (result.contains(".")) {
            result = result.substring(result.lastIndexOf(".") + 1);
        }
        if (result.contains("@")) {
            result = result.substring(result.lastIndexOf("@") + 1);
        }
        return result;
    }

    protected Map<String, ObjectName> getQueueMap(MBeanServerConnection mbsc) throws IOException, AttributeNotFoundException, InstanceNotFoundException, MBeanException,
            ReflectionException {
        if (queueMap != null) {
            return queueMap;
        }
        ObjectName drs = jmxHelper.getDomainRuntimeService();
        ObjectName config = (ObjectName) mbsc.getAttribute(drs, "DomainConfiguration");
        queueMap = new HashMap<>();
        ObjectName[] configJmsSystemResources = (ObjectName[]) mbsc.getAttribute(config, "JMSSystemResources");
        for (ObjectName configJmsSystemResource : configJmsSystemResources) {
            ObjectName configJmsResource = (ObjectName) mbsc.getAttribute(configJmsSystemResource, "JMSResource");
            ObjectName[] configQueues = (ObjectName[]) mbsc.getAttribute(configJmsResource, "Queues");
            for (ObjectName configQueue : configQueues) {
                String configQueueName = (String) mbsc.getAttribute(configQueue, "Name");
                queueMap.put(configQueueName, configQueue);
            }
            ObjectName[] configDDQueues = (ObjectName[]) mbsc.getAttribute(configJmsResource, "DistributedQueues");
            for (ObjectName configQueue : configDDQueues) {
                String configQueueName = (String) mbsc.getAttribute(configQueue, "Name");
                queueMap.put(configQueueName, configQueue);
            }
            ObjectName[] configUDDQueues = (ObjectName[]) mbsc.getAttribute(configJmsResource, "UniformDistributedQueues");
            for (ObjectName configQueue : configUDDQueues) {
                String configQueueName = (String) mbsc.getAttribute(configQueue, "Name");
                queueMap.put(configQueueName, configQueue);
            }
        }
        return queueMap;
    }

    protected javax.jms.Queue getQueue(String queueName) throws NamingException {
        return (javax.jms.Queue) lookupDestination(queueName);
    }

    protected Topic getTopic(String topicName) throws NamingException {
        return (Topic) lookupDestination(topicName);
    }

    protected String getJndiName(InternalJMSDestination internalJmsDestination) {
        String destinationJndi = internalJmsDestination.getProperty(PROPERTY_JNDI_NAME);
        return destinationJndi;
        //return "java:/" + StringUtils.replace(destinationJndi, ".", "/");
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

    protected ObjectName getMessageDestinationName(String source, String selector) {
        for (InternalJMSDestination internalJmsDestination : getInternalJMSDestinations(source)) {
            ObjectName objectName = internalJmsDestination.getProperty(PROPERTY_OBJECT_NAME);
            List<InternalJmsMessage> messages = getMessagesFromDestination(objectName, selector);
            if (!messages.isEmpty()) {
                return objectName;
            }
        }
        return null;
    }

    @Override
    public void deleteMessages(String source, String[] messageIds) {
        String selector = jmsSelectorUtil.getSelector(messageIds);
        deleteMessages(getMessageDestinationName(source, selector), selector);
    }

    @Override
    public InternalJmsMessage getMessage(String source, String messageId) {
        InternalJmsMessage internalJmsMessage = null;
        for (InternalJMSDestination internalJmsDestination : getInternalJMSDestinations(source)) {
            //String destinationType = internalJmsDestination.getType();
            //if ("Queue".equals(destinationType)) {
            try {
                ObjectName destination = internalJmsDestination.getProperty(PROPERTY_OBJECT_NAME);
                internalJmsMessage = getMessageFromDestination(destination, messageId);
                if (internalJmsMessage != null) break;
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            //}
            //throw new InternalJMSException("Unknown destination type [" + destinationType + "]");
        }
        return internalJmsMessage;
    }

    protected InternalJmsMessage getMessageFromDestination(ObjectName destination, String messageId) {
        List<InternalJmsMessage> messages = getMessagesFromDestination(destination, jmsSelectorUtil.getSelector(messageId));
        if (!messages.isEmpty()) {
            return messages.get(0);
        }
        return null;
    }

    protected List<InternalJmsMessage> getMessagesFromDestination(final ObjectName destination, final String selectorString) {
        return jmxTemplate.query(
                new JMXOperation() {
                    @Override
                    public List<InternalJmsMessage> execute(MBeanServerConnection mbsc) {
                        return doGetMessagesFromDestination(mbsc, selectorString, destination);
                    }
                }
        );
    }

    protected List<InternalJmsMessage> doGetMessagesFromDestination(MBeanServerConnection mbsc, String selectorString, ObjectName destination) {
        try {
            List<InternalJmsMessage> messages = new ArrayList<>();
            String selector = selectorString;
            if (selector == null) {
                selector = "true";
            }
            Integer timeout = new Integer(0);
            Integer stateMask = new Integer( // Show messages in destination in all possible states
                    MessageInfo.STATE_VISIBLE | // Visible and available for consumption
                            MessageInfo.STATE_DELAYED | // Pending delayed delivery
                            MessageInfo.STATE_PAUSED | // Pending pause operation
                            MessageInfo.STATE_RECEIVE | // Pending receive operation
                            MessageInfo.STATE_SEND | // Pending send operation
                            MessageInfo.STATE_TRANSACTION // Pending send or receive operation as part of global transaction
            );

            String messageCursor = (String) mbsc.invoke(
                    destination,
                    "browseMessages",
                    new Object[]{selector, timeout, stateMask},
                    new String[]{String.class.getName(), Integer.class.getName(), Integer.class.getName()});

            Long totalAmountOfMessages = (Long) mbsc.invoke(
                    destination,
                    "getCursorSize",
                    new Object[]{messageCursor}, new String[]{String.class.getName()});

            CompositeData[] allMessageMetaData = (CompositeData[]) mbsc.invoke(
                    destination,
                    "getItems",
                    new Object[]{messageCursor, new Long(0), new Integer(totalAmountOfMessages.intValue())},
                    new String[]{String.class.getName(), Long.class.getName(), Integer.class.getName()});

            if (allMessageMetaData != null) {
                for (CompositeData compositeData : allMessageMetaData) {
                    try {
                        InternalJmsMessage message = getInternalJmsMessage(destination, mbsc, messageCursor, compositeData);
                        messages.add(message);
                    } catch (Exception e) {
                        LOG.error("Error converting message [" + compositeData + "]", e);
                    }
                }
            }
            return messages;
        } catch (Exception e) {
            throw new InternalJMSException("Error getting messages from destination: " + destination, e);
        }
    }

    protected InternalJmsMessage getInternalJmsMessage(ObjectName destination, MBeanServerConnection mbsc, String messageCursor, CompositeData messageMetaData) throws ParserConfigurationException, SAXException, IOException, MBeanException, InstanceNotFoundException, ReflectionException {
        InternalJmsMessage message = convertMessage(messageMetaData);
        String messageId = message.getId();
        CompositeData messageDataDetails = (CompositeData) mbsc.invoke(destination, "getMessage", new Object[]{messageCursor, messageId}, new String[]{
                String.class.getName(), String.class.getName()});
        message = convertMessage(messageDataDetails);
        return message;
    }

    protected List<InternalJMSDestination> getInternalJMSDestinations(String source) {
        if (StringUtils.isEmpty(source)) {
            throw new InternalJMSException("Source has not been specified");
        }
        List<InternalJMSDestination> destinations = getDestinations().get(getShortDestName(source));
        if (destinations == null || destinations.isEmpty()) {
            throw new InternalJMSException("Could not find destination for [" + source + "]");
        }
        return destinations;
    }

    @Override
    public List<InternalJmsMessage> browseMessages(String source) {
        return browseMessages(source, null, null, null, null);
    }

    @Override
    public List<InternalJmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause) {

        List<InternalJmsMessage> internalJmsMessages = new ArrayList<>();
        for (InternalJMSDestination destination : getInternalJMSDestinations(source)) {
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
                    ObjectName jmsDestination = destination.getProperty(PROPERTY_OBJECT_NAME);
                    internalJmsMessages.addAll(getMessagesFromDestination(jmsDestination, selector));
                } catch (Exception e) {
                    throw new InternalJMSException("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
                }
            }
            throw new InternalJMSException("Unrecognized destination type [" + destinationType + "]");
        }
        return internalJmsMessages;
    }

    protected int deleteMessages(final ObjectName destination, final String selector) {
        return jmxTemplate.query(
                new JMXOperation() {
                    @Override
                    public Integer execute(MBeanServerConnection mbsc) {
                        return doDeleteMessages(mbsc, destination, selector);
                    }
                }
        );
    }

    protected Integer doDeleteMessages(MBeanServerConnection mbsc, ObjectName destination, String selector) {
        try {
            Integer deleted = (Integer) mbsc.invoke(destination, "deleteMessages", new Object[]{selector}, new String[]{String.class.getName()});
            return deleted;
        } catch (Exception e) {
            throw new InternalJMSException("Failed to build JMS destination map", e);
        }
    }

    @Override
    public void moveMessages(String sourceFrom, String sourceTo, String[] messageIds) {

        String selector = jmsSelectorUtil.getSelector(messageIds);
        ObjectName fromDestination = getMessageDestinationName(sourceFrom, selector);
        ObjectName toDestination = getMessageDestinationName(sourceTo, selector);
        moveMessages(fromDestination, toDestination, jmsSelectorUtil.getSelector(messageIds));

    }

    protected int moveMessages(final ObjectName from, final ObjectName to, final String selector) {
        return jmxTemplate.query(
                new JMXOperation() {
                    @Override
                    public Integer execute(MBeanServerConnection mbsc) {
                        return doMoveMessages(mbsc, to, from, selector);
                    }
                }
        );
    }

    protected Integer doMoveMessages(MBeanServerConnection mbsc, ObjectName to, ObjectName from, String selector) {
        try {
            CompositeData toDestinationInfo = (CompositeData) mbsc.getAttribute(to, "DestinationInfo");
            Integer moved = (Integer) mbsc.invoke(from, "moveMessages", new Object[]{selector, toDestinationInfo}, new String[]{String.class.getName(),
                    CompositeData.class.getName()});
            return moved;
        } catch (Exception e) {
            throw new InternalJMSException("Error moving messages", e);
        }
    }

    protected InternalJmsMessage convertMessage(CompositeData messageData) throws IOException, SAXException, ParserConfigurationException {
        InternalJmsMessage message = new InternalJmsMessage();
        String xmlMessage = String.valueOf(messageData.get("MessageXMLText"));
        Document xmlDocument = parseXML(xmlMessage);
        String ns = "http://www.bea.com/WLS/JMS/Message";
        Element root = xmlDocument.getDocumentElement();
        Element header = getChildElement(root, "Header");
        Element jmsMessageId = getChildElement(header, "JMSMessageID");
        String id = jmsMessageId.getTextContent();
        message.setId(id);
        message.getProperties().put("JMSMessageID", id);
        Element jmsTimestamp = getChildElement(header, "JMSTimestamp");
        String timestamp = jmsTimestamp.getTextContent();
        message.setTimestamp(new Date(Long.parseLong(timestamp)));
        message.getProperties().put("JMSTimestamp", timestamp);
        Element jmsType = getChildElement(header, "JMSType");
        if (jmsType != null) {
            String type = jmsType.getTextContent();
            message.setType(type);
            message.getProperties().put("JMSType", type);
        }
        Element propertiesRoot = getChildElement(header, "Properties");
        List<Element> properties = getChildElements(propertiesRoot, "property");
        for (Element property : properties) {
            String key = property.getAttribute("name");
            final Element firstChildElement = getFirstChildElement(property);
            String value = null;
            if (firstChildElement != null) {
                value = firstChildElement.getTextContent();
            }
            message.getProperties().put(key, value);
        }
        Element jmsBody = getChildElement(root, "Body");
        if (jmsBody != null) {
            message.setContent(StringUtils.trim(jmsBody.getTextContent()));
        }
        return message;
    }

    protected Document parseXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        factory.setNamespaceAware(true);
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    protected Element getChildElement(Element parent, String name) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) parent.getChildNodes().item(i);
                String localName = child.getLocalName();
                if (localName == null) {
                    localName = child.getNodeName();
                    if (localName != null && localName.contains(":")) {
                        localName = localName.substring(localName.indexOf(":") + 1);
                    }
                }
                if (StringUtils.equals(localName, name)) {
                    return child;
                }
            }
        }
        return null;
    }

    protected List<Element> getChildElements(Element parent, String name) {
        List<Element> childElements = new ArrayList<Element>();
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) parent.getChildNodes().item(i);
                String localName = child.getLocalName();
                if (localName == null) {
                    localName = child.getNodeName();
                    if (localName != null && localName.contains(":")) {
                        localName = localName.substring(localName.indexOf(":") + 1);
                    }
                }
                if (StringUtils.equals(localName, name)) {
                    childElements.add(child);
                }
            }
        }
        return childElements;
    }

    protected Element getFirstChildElement(Element parent) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) parent.getChildNodes().item(i);
                return child;
            }
        }
        return null;
    }
}
