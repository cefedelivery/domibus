package eu.domibus.plugin;

import eu.domibus.common.AuthRole;
import eu.domibus.common.NotificationType;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessageNotFoundException;
import org.apache.commons.lang.StringUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Christian Koch, Stefan Mueller
 */

public class NotificationListenerService implements MessageListener, JmsListenerConfigurer, MessageLister, eu.domibus.plugin.NotificationListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NotificationListenerService.class);
    protected static final String PROP_LIST_PENDING_MESSAGES_MAXCOUNT = "domibus.listPendingMessages.maxCount";

    @Autowired
    @Qualifier(value = "jmsTemplateNotify")
    private JmsOperations jmsOperations;

    @Autowired
    @Qualifier("internalJmsListenerContainerFactory")
    private JmsListenerContainerFactory jmsListenerContainerFactory;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    private Properties domibusProperties;

    private Queue backendNotificationQueue;
    private BackendConnector.Mode mode;
    private BackendConnector backendConnector;

    public NotificationListenerService(final Queue queue, final BackendConnector.Mode mode) {
        backendNotificationQueue = queue;
        this.mode = mode;
    }

    public void setBackendConnector(final BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Transactional
    public void onMessage(final Message message) {
        if (!authUtils.isUnsecureLoginAllowed())
            authUtils.setAuthenticationToSecurityContext("notif", "notif", AuthRole.ROLE_ADMIN);

        try {
            final String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            final NotificationType notificationType = NotificationType.valueOf(message.getStringProperty(MessageConstants.NOTIFICATION_TYPE));
            switch (notificationType) {
                case MESSAGE_RECEIVED:
                    backendConnector.deliverMessage(messageId);
                    break;
                case MESSAGE_SEND_FAILURE:
                    backendConnector.messageSendFailed(messageId);
                    break;
                case MESSAGE_SEND_SUCCESS:
                    backendConnector.messageSendSuccess(messageId);
                    break;
                case MESSAGE_RECEIVED_FAILURE:
                    backendConnector.messageReceiveFailed(messageId, message.getStringProperty(MessageConstants.ENDPOINT));

            }
        } catch (Exception e) {
            LOG.error("Error processing message", e);
            throw new RuntimeException("Error processing message", e);
        }
    }

    public Collection<String> listPendingMessages() {
        if (!authUtils.isUnsecureLoginAllowed())
            authUtils.hasUserOrAdminRole();

        String originalUser = authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
        LOG.info("Authorized as " + (originalUser == null ? "super user" : originalUser));

        /* if originalUser is null, all messages are returned */
        return getQueueElements(NotificationType.MESSAGE_RECEIVED, originalUser);
    }

    public Collection<String> listSendFailureMessages() {
        return getQueueElements(NotificationType.MESSAGE_SEND_FAILURE);
    }

    public Collection<String> listReceiveFailureMessages() {
        return getQueueElements(NotificationType.MESSAGE_RECEIVED_FAILURE);
    }

    private Collection<String> getQueueElements(final NotificationType notificationType) {
        return getQueueElements(notificationType, null);
    }


    private Collection<String> getQueueElements(final NotificationType notificationType, final String finalRecipient) {
        if (this.mode == BackendConnector.Mode.PUSH) {
            throw new UnsupportedOperationException("this method is only available for clients using Mode.PULL");
        }
        final Collection<String> result = browseQueue(notificationType, finalRecipient);
        return result;
    }

    private Collection<String> browseQueue(final NotificationType notificationType, final String finalRecipient) {
        final Collection<String> result = new ArrayList<>();

        final String strMaxPendingMessagesRetrieveCount = domibusProperties.getProperty(PROP_LIST_PENDING_MESSAGES_MAXCOUNT, "500");
        final int intMaxPendingMessagesRetrieveCount = Integer.parseInt(strMaxPendingMessagesRetrieveCount);
        LOG.debug("maxPendingMessagesRetrieveCount:" + intMaxPendingMessagesRetrieveCount);

        jmsOperations.browse(backendNotificationQueue, new BrowserCallback<Void>() {
            @Override
            public Void doInJms(final Session session, final QueueBrowser browser) throws JMSException {
                result.addAll(listFromQueue(notificationType, browser, finalRecipient, intMaxPendingMessagesRetrieveCount));
                return null;
            }
        });
        return result;
    }

    protected Collection<String> listFromQueue(NotificationType notificationType, QueueBrowser browser, String finalRecipient, int intMaxPendingMessagesRetrieveCount) throws JMSException {
        final Enumeration browserEnumeration = browser.getEnumeration();
        int countOfMessagesIncluded = 0;
        Collection<String> result = new ArrayList<>();
        while (browserEnumeration.hasMoreElements()) {
            final Message message = (Message) browserEnumeration.nextElement();
            if (notificationType.name().equals(message.getStringProperty(MessageConstants.NOTIFICATION_TYPE))) {
                if (finalRecipient == null
                        || (StringUtils.equals(finalRecipient, message.getStringProperty(MessageConstants.FINAL_RECIPIENT)))) {
                    String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
                    result.add(messageId);
                    countOfMessagesIncluded++;
                    LOG.trace("Added MessageId:" + messageId + " in listFromQueue!");
                    if ((intMaxPendingMessagesRetrieveCount != 0) && (countOfMessagesIncluded >= intMaxPendingMessagesRetrieveCount)) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeFromPending(final String messageId) throws MessageNotFoundException {
        if (!authUtils.isUnsecureLoginAllowed())
            authUtils.hasUserOrAdminRole();

        if (this.mode == BackendConnector.Mode.PUSH) {
            LOG.debug("No messages will be removed because this a PUSH consumer");
            return;
        }
        final String selector = MessageConstants.MESSAGE_ID + "= '" + messageId + "'";

        final Boolean received = jmsOperations.browseSelected(backendNotificationQueue, selector, new BrowserCallback<Boolean>() {
            @Override
            public Boolean doInJms(final Session session, final QueueBrowser browser) throws JMSException {
                return browser.getEnumeration().hasMoreElements();
            }
        });
        if (!received) {
            throw new MessageNotFoundException("No message with id [" + messageId + "] pending for download");
        }
        jmsOperations.receiveSelected(getBackendNotificationQueue(), selector);
    }


    @Override
    public void configureJmsListeners(final JmsListenerEndpointRegistrar registrar) {

        if (this.mode == BackendConnector.Mode.PUSH) {
            final SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
            endpoint.setId(getBackendName());
            final Queue pushQueue = backendNotificationQueue;
            if (pushQueue == null) {
                throw new ConfigurationException("No notification queue found for " + getBackendName());
            } else {
                try {
                    endpoint.setDestination(getQueueName(pushQueue));
                } catch (final JMSException e) {
                    LOG.error("Problem with predefined queue.", e);
                }
            }
            endpoint.setMessageListener(this);
            registrar.registerEndpoint(endpoint, jmsListenerContainerFactory);
        }
    }

    @Override
    public String getBackendName() {
        return backendConnector.getName();
    }

    @Override
    public Queue getBackendNotificationQueue() {
        return backendNotificationQueue;
    }

    protected String getQueueName(Queue queue) throws JMSException {
        return queue.getQueueName();
    }
}
