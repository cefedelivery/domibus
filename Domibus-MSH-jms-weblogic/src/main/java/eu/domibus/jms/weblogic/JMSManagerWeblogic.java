package eu.domibus.jms.weblogic;

import eu.domibus.jms.spi.JMSDestinationSPI;
import eu.domibus.jms.spi.JMSManagerSPI;
import eu.domibus.jms.spi.JmsMessageSPI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
@Component
public class JMSManagerWeblogic implements JMSManagerSPI {

    private static final Log LOG = LogFactory.getLog(JMSManagerWeblogic.class);

    private static final String PROPERTY_OBJECT_NAME = "ObjectName";
    private static final String PROPERTY_JNDI_NAME = "Jndi";

    protected Map<String, JMSDestinationSPI> destinationMap;
    protected Map<String, ObjectName> queueMap;
    protected Map<String, ObjectName> topicMap;

    @Autowired
    JMXHelper jmxHelper;

    @Override
    public Map<String, JMSDestinationSPI> getDestinations() {
        if (destinationMap != null) {
            return destinationMap;
        }
        destinationMap = new TreeMap<String, JMSDestinationSPI>();
        try {
            MBeanServerConnection mbsc = jmxHelper.getDomainRuntimeMBeanServerConnection();
            ObjectName drs = jmxHelper.getDomainRuntimeService();
            ObjectName config = (ObjectName) mbsc.getAttribute(drs, "DomainConfiguration");
            ObjectName[] servers = (ObjectName[]) mbsc.getAttribute(drs, "ServerRuntimes");
            for (ObjectName server : servers) {
                LOG.debug("Server " + server);
                String serverAddress = (String) mbsc.getAttribute(server, "ListenAddress");
                if (serverAddress.contains("/")) {
                    serverAddress = serverAddress.substring(0, serverAddress.indexOf("/"));
                }
                Integer serverPort = (Integer) mbsc.getAttribute(server, "ListenPort");
                ObjectName jms = (ObjectName) mbsc.getAttribute(server, "JMSRuntime");
                ObjectName[] jmsServers = (ObjectName[]) mbsc.getAttribute(jms, "JMSServers");
                for (ObjectName jmsServer : jmsServers) {
                    LOG.debug("JMS Server " + jmsServer);
                    ObjectName[] jmsDestinations = (ObjectName[]) mbsc.getAttribute(jmsServer, "Destinations");
                    for (ObjectName jmsDestination : jmsDestinations) {
                        LOG.debug("JMS Destination " + jmsDestination);
                        JMSDestinationSPI destination = new JMSDestinationSPI();
                        String destinationFQName = (String) mbsc.getAttribute(jmsDestination, "Name");
                        String moduleName = destinationFQName.substring(0, destinationFQName.indexOf("!"));
                        String destinationName = destinationFQName.substring(destinationFQName.indexOf("!") + 1);
                        if (destinationName.contains(".")) {
                            String jmsServerName = destinationName.substring(0, destinationName.lastIndexOf("."));
                            destinationName = destinationName.substring(destinationName.lastIndexOf(".") + 1);
                        }
                        if (destinationName.contains("@")) {
                            String jmsServerName = destinationName.substring(0, destinationName.lastIndexOf("@"));
                            destinationName = destinationName.substring(destinationName.lastIndexOf("@") + 1);
                        }
                        destination.setName(destinationName);
//                        destination.setServerAddress(serverAddress);
//                        destination.setServerPort(serverPort);
                        ObjectName configQueue = getQueueMap(mbsc).get(destinationName);
                        if (configQueue != null) {
                            destination.setType("Queue");
                            destination.setProperty(PROPERTY_OBJECT_NAME, jmsDestination);
//                            destination.setObjectName(jmsDestination);
                            String configQueueJndiName = (String) mbsc.getAttribute(configQueue, "JNDIName");
//                            destination.setJndiName(configQueueJndiName);
                            destination.setProperty(PROPERTY_JNDI_NAME, configQueueJndiName);
                        }
                        ObjectName configTopic = getTopicMap(mbsc).get(destinationName);
                        if (configTopic != null) {
                            destination.setProperty(PROPERTY_OBJECT_NAME, jmsDestination);
                            destination.setType("Topic");
                            String configTopicJndiName = (String) mbsc.getAttribute(configTopic, "JNDIName");
//                            destination.setJndiName(configTopicJndiName);
                            destination.setProperty(PROPERTY_JNDI_NAME, configTopicJndiName);
                            ObjectName[] jmsDurableSubscribers = (ObjectName[]) mbsc.getAttribute(jmsDestination, "DurableSubscribers");
                            for (ObjectName jmsDurableSubscriber : jmsDurableSubscribers) {
                                JMSDestinationSPI subscriberDestination = new JMSDestinationSPI();
                                subscriberDestination.setProperty(PROPERTY_OBJECT_NAME, jmsDurableSubscriber);
//                                subscriberDestination.setObjectName(jmsDurableSubscriber);
                                String subscriptionName = (String) mbsc.getAttribute(jmsDurableSubscriber, "SubscriptionName");
                                subscriberDestination.setName(destination.getName() + "_" + subscriptionName);
                                subscriberDestination.setType("Queue");
                                subscriberDestination.setProperty(PROPERTY_JNDI_NAME, null);
//                                subscriberDestination.setJndiName(null);
                                destinationMap.put(subscriberDestination.getName(), subscriberDestination);
                            }
                        }
                        Long numberOfMessages = (Long) mbsc.getAttribute(jmsDestination, "MessagesCurrentCount");
                        Long numberOfMessagesPending = (Long) mbsc.getAttribute(jmsDestination, "MessagesPendingCount");

                        destination.setNumberOfMessages(numberOfMessages);
                        destination.setNumberOfMessagesPending(numberOfMessagesPending);
                        destinationMap.put(destination.getName(), destination);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to build JMS destination map", e);
        }
        return destinationMap;
    }

    protected Map<String, ObjectName> getQueueMap(MBeanServerConnection mbsc) throws IOException, AttributeNotFoundException, InstanceNotFoundException, MBeanException,
            ReflectionException {
        if (queueMap != null) {
            return queueMap;
        }
        ObjectName drs = jmxHelper.getDomainRuntimeService();
        ObjectName config = (ObjectName) mbsc.getAttribute(drs, "DomainConfiguration");
        queueMap = new HashMap<String, ObjectName>();
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

    protected Map<String, ObjectName> getTopicMap(MBeanServerConnection mbsc) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException,
            ReflectionException, IOException {
        if (topicMap != null) {
            return topicMap;
        }
        topicMap = new HashMap<String, ObjectName>();
        ObjectName drs = jmxHelper.getDomainRuntimeService();
        ObjectName config = (ObjectName) mbsc.getAttribute(drs, "DomainConfiguration");
        ObjectName[] configJmsSystemResources = (ObjectName[]) mbsc.getAttribute(config, "JMSSystemResources");
        for (ObjectName configJmsSystemResource : configJmsSystemResources) {
            ObjectName configJmsResource = (ObjectName) mbsc.getAttribute(configJmsSystemResource, "JMSResource");
            ObjectName[] configTopics = (ObjectName[]) mbsc.getAttribute(configJmsResource, "Topics");
            for (ObjectName configTopic : configTopics) {
                String configTopicName = (String) mbsc.getAttribute(configTopic, "Name");
                topicMap.put(configTopicName, configTopic);
            }
        }
        return topicMap;
    }

    @Override
    public boolean sendMessage(JmsMessageSPI message, String connectionFactory, String destination, String destinationType) {
        return false;
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

    private int deleteMessages(ObjectName destination, String selector) throws Exception {
        MBeanServerConnection mbsc = jmxHelper.getDomainRuntimeMBeanServerConnection();
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
        MBeanServerConnection mbsc = jmxHelper.getDomainRuntimeMBeanServerConnection();
        CompositeData toDestinationInfo = (CompositeData) mbsc.getAttribute(to, "DestinationInfo");
        Integer moved = (Integer) mbsc.invoke(from, "moveMessages", new Object[] { selector, toDestinationInfo }, new String[] { String.class.getName(),
                CompositeData.class.getName() });
        return moved;
    }
}
