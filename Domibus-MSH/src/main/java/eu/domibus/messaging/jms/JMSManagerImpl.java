package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.services.AuditService;
import eu.domibus.jms.spi.InternalJMSManager;
import eu.domibus.jms.spi.InternalJmsMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
@Transactional
public class JMSManagerImpl implements JMSManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSManagerImpl.class);

    @Autowired
    InternalJMSManager internalJmsManager;

    @Autowired
    JMSDestinationMapper jmsDestinationMapper;

    @Autowired
    JMSMessageMapper jmsMessageMapper;

    @Autowired
    private AuditService auditService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    public Map<String, JMSDestination> getDestinations() {
        return jmsDestinationMapper.convert(internalJmsManager.findDestinationsGroupedByFQName());
    }

    @Override
    public JmsMessage getMessage(String source, String messageId) {
        InternalJmsMessage internalJmsMessage = internalJmsManager.getMessage(source, messageId);
        return jmsMessageMapper.convert(internalJmsMessage);
    }

    @Override
    public List<JmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selector) {

        Domain domain = domainContextProvider.getCurrentDomain();
        if(domain != null) {
            selector = (selector == null ? "" : (selector + " and ")) + MessageConstants.DOMAIN + "='" + domain.getCode() + "'";
        }

        List<InternalJmsMessage> messagesSPI = internalJmsManager.browseMessages(source, jmsType, fromDate, toDate, selector);
        return jmsMessageMapper.convert(messagesSPI);
    }

    @Override
    public List<JmsMessage> browseMessages(String source) {
        return browseMessages(source, null, null, null, null);
    }

    @Override
    public List<JmsMessage> browseMessages(String source, String selector) {
        return browseMessages(source, null, null, null, selector);
    }

    @Override
    public void sendMessageToQueue(JmsMessage message, String destination) {
        sendMessageToQueue(message, destination, InternalJmsMessage.MessageType.TEXT_MESSAGE);
    }

    @Override
    public void sendMapMessageToQueue(JmsMessage message, String destination) {
        sendMessageToQueue(message, destination, InternalJmsMessage.MessageType.MAP_MESSAGE);
    }

    protected void sendMessageToQueue(JmsMessage message, String destination, InternalJmsMessage.MessageType messageType) {
        message.getProperties().put(JmsMessage.PROPERTY_ORIGINAL_QUEUE, destination);
        final Domain currentDomain = domainContextProvider.getCurrentDomain();
        message.getProperties().put(MessageConstants.DOMAIN, currentDomain.getCode());
        InternalJmsMessage internalJmsMessage = jmsMessageMapper.convert(message);
        internalJmsMessage.setMessageType(messageType);
        internalJmsManager.sendMessage(internalJmsMessage, destination);
    }

    @Override
    public void sendMessageToQueue(JmsMessage message, Queue destination) {
        sendMessageToQueue(message, destination, InternalJmsMessage.MessageType.TEXT_MESSAGE);
    }

    @Override
    public void sendMapMessageToQueue(JmsMessage message, Queue destination) {
        sendMessageToQueue(message, destination, InternalJmsMessage.MessageType.MAP_MESSAGE);
    }

    protected void sendMessageToQueue(JmsMessage message, Queue destination, InternalJmsMessage.MessageType messageType) {
        try {
            message.getProperties().put(JmsMessage.PROPERTY_ORIGINAL_QUEUE, destination.getQueueName());
        } catch (JMSException e) {
            LOG.warn("Could not add the property [" + JmsMessage.PROPERTY_ORIGINAL_QUEUE + "] on the destination", e);
        }
        sendMessageToDestination(message, destination, messageType);
    }

    protected void sendMessageToDestination(JmsMessage message, Destination destination, InternalJmsMessage.MessageType messageType) {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();
        message.getProperties().put(MessageConstants.DOMAIN, currentDomain.getCode());
        InternalJmsMessage internalJmsMessage = jmsMessageMapper.convert(message);
        internalJmsMessage.setMessageType(messageType);
        internalJmsManager.sendMessage(internalJmsMessage, destination);
    }

    @Override
    public void sendMessageToTopic(JmsMessage message, Topic destination) {
        sendMessageToDestination(message, destination, InternalJmsMessage.MessageType.TEXT_MESSAGE);
    }

    @Override
    public void deleteMessages(String source, String[] messageIds) {
        internalJmsManager.deleteMessages(source, messageIds);
        Arrays.asList(messageIds).forEach(m -> auditService.addJmsMessageDeletedAudit(m, source));
    }

    @Override
    public void moveMessages(String source, String destination, String[] messageIds) {
        internalJmsManager.moveMessages(source, destination, messageIds);
        Arrays.asList(messageIds).forEach(m -> auditService.addJmsMessageMovedAudit(m, source, destination));
    }

    @Override
    public JmsMessage consumeMessage(String source, String messageId) {
        InternalJmsMessage internalJmsMessage = internalJmsManager.consumeMessage(source, messageId);
        return jmsMessageMapper.convert(internalJmsMessage);
    }
}
