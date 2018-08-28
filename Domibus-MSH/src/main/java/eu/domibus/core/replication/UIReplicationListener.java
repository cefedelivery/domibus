package eu.domibus.core.replication;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * JMS listener for the queue {@code domibus.jms.queue.ui.replication}
 *
 * @author Catalin Enache
 * @since 4.0
 */
@Component
public class UIReplicationListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationListener.class);

    @Autowired
    protected UIReplicationDataService uiReplicationDataService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @JmsListener(destination = "${domibus.jms.queue.ui.replication}", containerFactory = "uiReplicationJmsListenerContainerFactory")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processUIReplication(final MapMessage map) throws JMSException {
        final String messageId = map.getStringProperty(MessageConstants.MESSAGE_ID);
        final String domainCode = map.getStringProperty(MessageConstants.DOMAIN);

        LOG.debug("Sending message ID [{}] for domain [{}]", messageId, domainCode);
        domainContextProvider.setCurrentDomain(domainCode);

        final String jmsType = map.getJMSType();
        LOG.debug("processUIReplication for jmsType=[{}]", jmsType);

        switch (UIJMSType.valueOf(jmsType)) {
            case USER_MESSAGE_RECEIVED:
                uiReplicationDataService.messageReceived(messageId, map.getJMSTimestamp());
                break;
            case USER_MESSAGE_SUBMITTED:
                uiReplicationDataService.messageSubmitted(messageId, map.getJMSTimestamp());
                break;
            case MESSAGE_STATUS_CHANGE:
                MessageStatus messageStatus = MessageStatus.valueOf(map.getStringProperty(UIReplicationSignalService.JMS_PROP_STATUS));
                uiReplicationDataService.messageStatusChange(messageId, messageStatus, map.getJMSTimestamp());
                break;
            case MESSAGE_NOTIFICATION_STATUS_CHANGE:
                NotificationStatus notificationStatus = NotificationStatus.valueOf(map.getStringProperty(UIReplicationSignalService.JMS_PROP_NOTIF_STATUS));
                uiReplicationDataService.messageNotificationStatusChange(messageId, notificationStatus, map.getJMSTimestamp());
                break;
            case MESSAGE_CHANGE:
                uiReplicationDataService.messageChange(messageId, map.getJMSTimestamp());
                break;
            case SIGNAL_MESSAGE_SUBMITTED:
                uiReplicationDataService.signalMessageSubmitted(messageId, map.getJMSTimestamp());
                break;
            case SIGNAL_MESSAGE_RECEIVED:
                uiReplicationDataService.signalMessageReceived(messageId, map.getJMSTimestamp());
                break;
            default:
                throw new AssertionError("Invalid UIJMSType enum value");
        }
    }
}
