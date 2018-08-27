package eu.domibus.core.replication;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

/**
 * Signals creation or update of a User or Signal message
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class UIReplicationSignalService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationSignalService.class);

    @Autowired
    @Qualifier("uiReplicationQueue")
    private Queue uiReplicationQueue;

    @Autowired
    protected JMSManager jmsManager;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    public void userMessageReceived(String messageId) {
        if (!isReplicationEnabled()) {
            return;
        }
        final JmsMessage message = createJMSMessage(messageId, UIJMSType.USER_MESSAGE_RECEIVED);

        jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
    }

    public void messageStatusChange(String messageId) {
        if (!isReplicationEnabled()) {
            return;
        }
        final JmsMessage message = createJMSMessage(messageId, UIJMSType.MESSAGE_STATUS_CHANGE);

        jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
    }

    public void messageChange(String messageId) {
        if (!isReplicationEnabled()) {
            return;
        }
        final JmsMessage message = createJMSMessage(messageId, UIJMSType.MESSAGE_CHANGE);

        jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
    }

    public void messageNotificationStatusChange(String messageId) {
        if (!isReplicationEnabled()) {
            return;
        }
        final JmsMessage message = createJMSMessage(messageId, UIJMSType.MESSAGE_NOTIFICATION_STATUS_CHANGE);

        jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
    }

    public void userMessageSubmitted(String messageId) {
        if (!isReplicationEnabled()) {
            return;
        }
        final JmsMessage message = createJMSMessage(messageId, UIJMSType.USER_MESSAGE_SUBMITTED);

        jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
    }

    public void signalMessageSubmitted(String messageId) {
        if (!isReplicationEnabled()) {
            return;
        }
        final JmsMessage message = createJMSMessage(messageId, UIJMSType.SIGNAL_MESSAGE_SUBMITTED);

        jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
    }

    public void signalMessageReceived(String messageId) {
        if (!isReplicationEnabled()) {
            return;
        }
        final JmsMessage message = createJMSMessage(messageId, UIJMSType.SIGNAL_MESSAGE_RECEIVED);

        jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
    }

    private JmsMessage createJMSMessage(String messageId, UIJMSType uiJMSType) {
        return JMSMessageBuilder.create()
                .type(uiJMSType.name())
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property(MessageConstants.DOMAIN, domainContextProvider.getCurrentDomain().getCode())
                .build();
    }

    public boolean isReplicationEnabled() {
        boolean uiReplicationEnabled = Boolean.parseBoolean(domibusPropertyProvider.getProperty("domibus.ui.replication.enabled", "false"));

        if (!uiReplicationEnabled) {
            LOG.debug("UIReplication is disabled - no processing will occur");
        }
        return uiReplicationEnabled;
    }
}
