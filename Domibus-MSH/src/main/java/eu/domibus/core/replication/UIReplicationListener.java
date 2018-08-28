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
    private UIReplicationDataService uiReplicationDataService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @JmsListener(destination = "${domibus.jms.queue.ui.replication}", containerFactory = "uiReplicationJmsListenerContainerFactory")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processUIReplication(final MapMessage map) throws JMSException {

        final String domainCode = map.getStringProperty(MessageConstants.DOMAIN);
        domainContextProvider.setCurrentDomain(domainCode);

        //disabling read of JMS messages
        if (!uiReplicationSignalService.isReplicationEnabled()) {
            LOG.debug("UIReplication is disabled - no processing will occur");
            return;
        }
        final String messageId = map.getStringProperty(MessageConstants.MESSAGE_ID);
        final String jmsType = map.getJMSType();

        LOG.debug("processUIReplication for messageId=[{}] domain=[{}] jmsType=[{}]", jmsType, messageId, domainCode);

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
