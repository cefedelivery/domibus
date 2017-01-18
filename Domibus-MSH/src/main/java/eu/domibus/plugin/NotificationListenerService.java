/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.plugin;

import eu.domibus.common.*;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessageNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
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
import org.springframework.util.ClassUtils;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

/**
 * @author Christian Koch, Stefan Mueller
 */

public class NotificationListenerService implements MessageListener, JmsListenerConfigurer, MessageLister, eu.domibus.plugin.NotificationListener {

    private static final Log LOG = LogFactory.getLog(NotificationListenerService.class);

    @Autowired
    @Qualifier(value = "jmsTemplateNotify")
    private JmsOperations jmsOperations;

    @Autowired
    @Qualifier("internalJmsListenerContainerFactory")
    private JmsListenerContainerFactory jmsListenerContainerFactory;

    @Autowired
    AuthUtils authUtils;

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
        } catch (Exception e) {
            LOG.error("Error processing message", e);
            throw new RuntimeException("Error processing message", e);
        }
    }

    protected boolean isNewMessageReceiveFailureDefined() throws Exception {
        final String targetClassName = getTargetObject(backendConnector);
        final Class<?> pluginImplementationClass = Thread.currentThread().getContextClassLoader().loadClass(targetClassName);
        boolean isNewMessageReceiveFailureDefined = true;
        try {
            pluginImplementationClass.getDeclaredMethod("messageReceiveFailed", MessageReceiveFailureEvent.class);
        } catch (NoSuchMethodException e) {
            LOG.debug("New messageReceiveFailed(MessageReceiveFailureEvent.class) is not defined");
            isNewMessageReceiveFailureDefined = false;
        }

        return isNewMessageReceiveFailureDefined;
    }

    protected String getTargetObject(Object proxy) throws Exception {
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return ((Advised) proxy).getTargetSource().getTarget().getClass().getCanonicalName();
        } else if (AopUtils.isCglibProxy(proxy)) {
            return ClassUtils.getUserClass(proxy).getCanonicalName();
        } else {
            return proxy.getClass().getCanonicalName();
        }
    }

    //TODO move this method to a delegate service
    protected void doMessageReceiveFailure(final Message message) throws JMSException {
        boolean newMessageReceiveFailureDefined = false;
        try {
            newMessageReceiveFailureDefined = isNewMessageReceiveFailureDefined();
        } catch (Exception e) {
            LOG.warn("Could not determine which variant of messageReceiveFailure method should be called");
        }
        final String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
        if (newMessageReceiveFailureDefined) {
            LOG.info("Calling messageReceiveFailed method");
            MessageReceiveFailureEvent event = new MessageReceiveFailureEvent();
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
            backendConnector.messageReceiveFailed(event);
        } else {
            LOG.info("Calling deprecated messageReceiveFailed method");
            backendConnector.messageReceiveFailed(messageId, message.getStringProperty(MessageConstants.ENDPOINT));
        }
    }

    public final Collection<String> listPendingMessages() {
        if (!authUtils.isUnsecureLoginAllowed())
            authUtils.hasUserOrAdminRole();

        String originalUser = authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
        LOG.info("Authorized as " + (originalUser == null ? "super user" : originalUser));

        /* if originalUser is null, all messages are returned */
        return getQueueElements(NotificationType.MESSAGE_RECEIVED, originalUser);
    }

    public final Collection<String> listSendFailureMessages() {
        return getQueueElements(NotificationType.MESSAGE_SEND_FAILURE);
    }

    public final Collection<String> listReceiveFailureMessages() {
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
        jmsOperations.browse(backendNotificationQueue, new BrowserCallback<Void>() {
            @Override
            public Void doInJms(final Session session, final QueueBrowser browser) throws JMSException {
                final Enumeration browserEnumeration = browser.getEnumeration();
                while (browserEnumeration.hasMoreElements()) {
                    final Message message = (Message) browserEnumeration.nextElement();
                    if (notificationType.name().equals(message.getStringProperty(MessageConstants.NOTIFICATION_TYPE))) {
                        if (finalRecipient == null ||
                                (finalRecipient != null && finalRecipient.equals(message.getStringProperty(MessageConstants.FINAL_RECIPIENT)))) {
                            result.add(message.getStringProperty(MessageConstants.MESSAGE_ID));
                        }
                    }
                }
                return null;
            }
        });
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
