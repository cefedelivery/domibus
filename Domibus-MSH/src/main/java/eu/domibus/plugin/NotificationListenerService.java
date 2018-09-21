
package eu.domibus.plugin;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.*;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.delegate.BackendConnectorDelegate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    private Queue backendNotificationQueue;
    private BackendConnector.Mode mode;
    private BackendConnector backendConnector;
    private List<NotificationType> requiredNotifications = null;

    /* Default notifications sent to the plugins, depending on their MODE (PULL or PUSH)
     * On PULL mode we do not notify for MESSAGE_SEND_SUCCESS and
     * MESSAGE_STATUS_CHANGE as there are too many notifications that pile up in the queue
     * This default list is used only when there is no requiredNotifications list declared in the plugin xml
     */
    @PostConstruct
    protected void initRequiredNotificationsList() {
        if (requiredNotifications != null) {
            LOG.debug("Required notifications already initialized [{}]", requiredNotifications);
            return;
        }
        requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);
        requiredNotifications.add(NotificationType.MESSAGE_SEND_FAILURE);
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED_FAILURE);

        if (BackendConnector.Mode.PUSH.equals(getMode())) {
            requiredNotifications.add(NotificationType.MESSAGE_SEND_SUCCESS);
            requiredNotifications.add(NotificationType.MESSAGE_STATUS_CHANGE);
        }
    }

    public NotificationListenerService(final Queue queue, final BackendConnector.Mode mode) {
        backendNotificationQueue = queue;
        this.mode = mode;
    }

    public NotificationListenerService(final Queue queue, final BackendConnector.Mode mode, final List<NotificationType> requiredNotifications) {
        backendNotificationQueue = queue;
        this.mode = mode;
        this.requiredNotifications = requiredNotifications;
    }

    public void setBackendConnector(final BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @MDCKey({DomibusLogger.MDC_MESSAGE_ID})
    @Transactional
    public void onMessage(final Message message) {
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("notif", "notif", AuthRole.ROLE_ADMIN);
        }

        try {
            final String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);

            final String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            LOG.debug("Processing message ID [{}] for domain [{}]", messageId, domainCode);
            domainContextProvider.setCurrentDomain(domainCode);

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
                    break;
                case MESSAGE_STATUS_CHANGE:
                    doMessageStatusChange(message);
                    break;
            }
        } catch (JMSException jmsEx) {
            LOG.error("Error getting the property from JMS message", jmsEx);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Error getting the property from JMS message", jmsEx.getCause());
        }
    }

    protected void doMessageStatusChange(final Message message) throws JMSException {
        MessageStatusChangeEvent event = new MessageStatusChangeEvent();
        final String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
        event.setMessageId(messageId);

        final String fromStatus = message.getStringProperty("fromStatus");
        if (StringUtils.isNotEmpty(fromStatus)) {
            event.setFromStatus(MessageStatus.valueOf(fromStatus));
        }
        event.setToStatus(MessageStatus.valueOf(message.getStringProperty("toStatus")));
        event.setChangeTimestamp(new Timestamp(message.getLongProperty("changeTimestamp")));
        event.addProperty("service", message.getStringProperty("service"));
        event.addProperty("serviceType", message.getStringProperty("serviceType"));
        event.addProperty("action", message.getStringProperty("action"));
        backendConnectorDelegate.messageStatusChanged(backendConnector, event);
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

        String originalUser = authUtils.getOriginalUserFromSecurityContext();
        LOG.info("Authorized as " + (originalUser == null ? "super user" : originalUser));

        /* if originalUser is null, all messages are returned */
        return getQueueElements(NotificationType.MESSAGE_RECEIVED, originalUser);
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
        final String strMaxPendingMessagesRetrieveCount = domibusPropertyProvider.getProperty(PROP_LIST_PENDING_MESSAGES_MAXCOUNT, "500");
        final int intMaxPendingMessagesRetrieveCount = Integer.parseInt(strMaxPendingMessagesRetrieveCount);
        LOG.debug("maxPendingMessagesRetrieveCount:" + intMaxPendingMessagesRetrieveCount);

        String selector = MessageConstants.NOTIFICATION_TYPE + "='" + notificationType.name() + "'";

        if (finalRecipient != null) {
            selector += " and " + MessageConstants.FINAL_RECIPIENT + "='" + finalRecipient + "'";
        }
        selector = jmsManager.getDomainSelector(selector);

        List<JmsMessage> messages;
        try {
            messages = jmsManager.browseClusterMessages(backendNotificationQueue.getQueueName(), selector);
        } catch (JMSException jmsEx) {
            LOG.error("Error trying to read the queue name", jmsEx);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not get the queue name", jmsEx.getCause());
        }

        int countOfMessagesIncluded = 0;
        for (JmsMessage message : messages) {
            String messageId = message.getCustomStringProperty(MessageConstants.MESSAGE_ID);
            result.add(messageId);
            countOfMessagesIncluded++;
            LOG.debug("Added MessageId [" + messageId + "]");
            if ((intMaxPendingMessagesRetrieveCount != 0) && (countOfMessagesIncluded >= intMaxPendingMessagesRetrieveCount)) {
                LOG.info("Limit of pending messages to return has been reached [" + countOfMessagesIncluded + "]");
                break;
            }
        }

        return result;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void removeFromPending(final String messageId) throws MessageNotFoundException {

        if (!authUtils.isUnsecureLoginAllowed())
            authUtils.hasUserOrAdminRole();

        if (this.mode == BackendConnector.Mode.PUSH) {
            LOG.debug("No messages will be removed because this a PUSH consumer");
            return;
        }

        //add messageId to MDC map
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }

        String queueName;
        try {
            queueName = backendNotificationQueue.getQueueName();
        } catch (JMSException jmsEx) {
            LOG.error("Error trying to get the queue name", jmsEx);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not get the queue name", jmsEx.getCause());
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

    @Override
    public BackendConnector.Mode getMode() {
        return this.mode;
    }

    @Override
    public List<NotificationType> getRequiredNotificationTypeList() {
        return this.requiredNotifications;
    }
}
