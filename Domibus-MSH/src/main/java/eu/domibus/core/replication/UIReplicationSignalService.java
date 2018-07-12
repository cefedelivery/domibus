package eu.domibus.core.replication;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.MessageStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class UIReplicationSignalService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationSignalService.class);
    static final String JMS_PROPERTY_STATUS = "status";

    @Autowired
    @Qualifier("domibusUIReplicationQueue")
    private Queue domibusUIReplicationQueue;

    @Autowired
    protected JMSManager jmsManager;

    public void userMessageReceived(String messageId) {
        final JmsMessage message = JMSMessageBuilder.create()
                .type(UIJMSType.USER_MESSAGE_RECEIVED.name())
                .property(MessageConstants.MESSAGE_ID, messageId).build();

        jmsManager.sendMapMessageToQueue(message, domibusUIReplicationQueue);
    }


    public void messageStatusChange(String messageId, MessageStatus newStatus) {
        final JmsMessage message = JMSMessageBuilder.create()
                .type(UIJMSType.MESSAGE_STATUS_CHANGE.name())
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property(JMS_PROPERTY_STATUS, newStatus.name()).build();

        jmsManager.sendMapMessageToQueue(message, domibusUIReplicationQueue);
    }

    public void messageChange(String messageId) {
        final JmsMessage message = JMSMessageBuilder.create()
                .type(UIJMSType.MESSAGE_CHANGE.name())
                .property(MessageConstants.MESSAGE_ID, messageId)
                .build();

        jmsManager.sendMapMessageToQueue(message, domibusUIReplicationQueue);
    }

    public void userMessageSubmitted(String messageId) {
        final JmsMessage message = JMSMessageBuilder.create()
                .type(UIJMSType.USER_MESSAGE_SUBMITTED.name())
                .property(MessageConstants.MESSAGE_ID, messageId).build();

        jmsManager.sendMapMessageToQueue(message, domibusUIReplicationQueue);
    }

    public void signalMessageSubmitted(String messageId) {
        final JmsMessage message = JMSMessageBuilder.create()
                .type(UIJMSType.SIGNAL_MESSAGE_SUBMITTED.name())
                .property(MessageConstants.MESSAGE_ID, messageId).build();

        jmsManager.sendMapMessageToQueue(message, domibusUIReplicationQueue);
    }

    public void signalMessageReceived(String messageId) {
        final JmsMessage message = JMSMessageBuilder.create()
                .type(UIJMSType.SIGNAL_MESSAGE_RECEIVED.name())
                .property(MessageConstants.MESSAGE_ID, messageId).build();

        jmsManager.sendMapMessageToQueue(message, domibusUIReplicationQueue);
    }

}
