package eu.domibus.plugin;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.AuthRole;
import eu.domibus.common.NotificationType;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessageNotFoundException;
import org.apache.commons.lang.StringUtils;
import eu.domibus.plugin.delegate.BackendConnectorDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class NotificationListenerService implements MessageListener, JmsListenerConfigurer, MessageLister, eu.domibus.plugin.NotificationListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NotificationListenerService.class);

    protected static final String PROP_LIST_PENDING_MESSAGES_MAXCOUNT = "domibus.listPendingMessages.maxCount";

    @Autowired
    private JMSManager jmsManager;

    @Autowired
    @Qualifier("internalJmsListenerContainerFactory")
    private JmsListenerContainerFactory jmsListenerContainerFactory;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private BackendConnectorDelegate backendConnectorDelegate;

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
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("notif", "notif", AuthRole.ROLE_ADMIN);
        }

        try {
            final String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            final NotificationType notificationType = NotificationType.valueOf(message.getStringProperty(MessageConstants.NOTIFICATION_TYPE));

            LOG.info("Received message with messageId [" + messageId + "] and notification type [" + notificationType + "]");

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
                    doMessageReceiveFailure(message);

            }
        } catch (JMSException jmsEx) {
            LOG.error("Error getting the property from JMS message", jmsEx);
            // TODO to be changed with something like the new DomibusCoreException
            throw new RuntimeException("Error getting the property from JMS message", jmsEx);
        }
    }


    protected void doMessageReceiveFailure(final Message message) throws JMSException {
        MessageReceiveFailureEvent event = new MessageReceiveFailureEvent();
        final String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
        event.setMessageId(messageId);
        final String errorCode = message.getStringProperty(MessageConstants.ERROR_CODE);
        final String errorDetail = message.getStringProperty(MessageConstants.ERROR_DETAIL);
        ErrorResultImpl errorResult = new ErrorResultImpl();
        try {
            errorResult.setErrorCode(ErrorCode.findBy(errorCode));
        } catch (IllegalArgumentException e) {
            LOG.warn("Could not find error code for [" + errorCode + "]");
        }
        errorResult.setErrorDetail(errorDetail);
        errorResult.setMessageInErrorId(messageId);
        event.setErrorResult(errorResult);
        event.setEndpoint(message.getStringProperty(MessageConstants.ENDPOINT));
        backendConnectorDelegate.messageReceiveFailed(backendConnector, event);
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

    protected Collection<String> browseQueue(final NotificationType notificationType, final String finalRecipient) {

        final Collection<String> result = new ArrayList<>();
        final String strMaxPendingMessagesRetrieveCount = domibusProperties.getProperty(PROP_LIST_PENDING_MESSAGES_MAXCOUNT, "500");
        final int intMaxPendingMessagesRetrieveCount = Integer.parseInt(strMaxPendingMessagesRetrieveCount);
        LOG.debug("maxPendingMessagesRetrieveCount:" + intMaxPendingMessagesRetrieveCount);

        List<JmsMessage> messages;
        try {
            messages = jmsManager.browseMessages(backendNotificationQueue.getQueueName());
        } catch (JMSException jmsEx) {
            LOG.error("Error trying to read the queue name", jmsEx);
            // TODO to be changed with something like the new DomibusCoreException
            throw new RuntimeException("Queue name error", jmsEx.getCause());
        }

        int countOfMessagesIncluded = 0;
        for (JmsMessage message : messages) {
            if (notificationType.name().equals(message.getCustomStringProperty(MessageConstants.NOTIFICATION_TYPE))) {
                if (finalRecipient == null || (StringUtils.equals(finalRecipient, message.getStringProperty(MessageConstants.FINAL_RECIPIENT)))) {
                    String messageId = message.getCustomStringProperty(MessageConstants.MESSAGE_ID);
                    result.add(messageId);
                    countOfMessagesIncluded++;
                    LOG.debug("Added MessageId [" + messageId + "]");
                    if ((intMaxPendingMessagesRetrieveCount != 0) && (countOfMessagesIncluded >= intMaxPendingMessagesRetrieveCount)) {
                        LOG.info("Limit of pending messages to return has been reached [" + countOfMessagesIncluded + "]");
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

        String queueName;
        try {
            queueName = backendNotificationQueue.getQueueName();
        } catch (JMSException jmsEx) {
            LOG.error("Error trying to get the queue name", jmsEx);
            // TODO to be changed with something like the new DomibusCoreException
            throw new RuntimeException("Queue name error", jmsEx.getCause());
        }

        JmsMessage message = jmsManager.consumeMessage(queueName, messageId);
        if (message == null) {
            LOG.businessError(DomibusMessageCode.BUS_MSG_NOT_FOUND, messageId);
            throw new MessageNotFoundException("No message with id [" + messageId + "] pending for download");
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MSG_CONSUMED, messageId, queueName);

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
