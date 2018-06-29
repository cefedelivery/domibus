package eu.domibus.core.replication;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.MessageStatus;
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

    @Autowired
    @Qualifier("domibusUIReplicationQueue")
    private Queue domibusUIReplicationQueue;

    @Autowired
    protected JMSManager jmsManager;

    public void signalMessageReceived(String messageId) {
        final JmsMessage messageReceived = JMSMessageBuilder.create()
                .type("messageReceived")
                .property(MessageConstants.MESSAGE_ID, messageId).build();

        jmsManager.sendMapMessageToQueue(messageReceived, domibusUIReplicationQueue);
    }


    public void signalMessageStatusChange(String messageId, MessageStatus newStatus) {
        final JmsMessage messageReceived = JMSMessageBuilder.create()
                .type("messageStatusChange")
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property("status", newStatus.name()).build();

        jmsManager.sendMapMessageToQueue(messageReceived, domibusUIReplicationQueue);
    }

    public void signalMessageSubmitted(String messageId) {
        final JmsMessage messageReceived = JMSMessageBuilder.create()
                .type("messageSubmitted")
                .property(MessageConstants.MESSAGE_ID, messageId).build();

        jmsManager.sendMapMessageToQueue(messageReceived, domibusUIReplicationQueue);
    }
}
