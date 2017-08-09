package eu.domibus.plugin.delegate;

import eu.domibus.api.util.ClassUtil;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@Component
public class DefaultBackendConnectorDelegate implements BackendConnectorDelegate {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultBackendConnectorDelegate.class);

    @Autowired
    ClassUtil classUtil;

    @Override
    public void messageStatusChanged(BackendConnector backendConnector, MessageStatusChangeEvent event) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Notifying connector about status change event [{}]", event);
        }
        backendConnector.messageStatusChanged(event);
    }

    @Override
    public void messageReceiveFailed(BackendConnector backendConnector, MessageReceiveFailureEvent event) {
        boolean newMessageReceiveFailureDefined = false;
        try {
            newMessageReceiveFailureDefined = isNewMessageReceiveFailureDefined(backendConnector);
        } catch (Exception e) {
            LOG.warn("Could not determine which variant of messageReceiveFailure method should be called. The deprecated messageReceiveFailure method will be called");
        }
        final String messageId = event.getMessageId();
        if (newMessageReceiveFailureDefined) {
            LOG.info("Calling messageReceiveFailed method");
            backendConnector.messageReceiveFailed(event);
        } else {
            LOG.info("Calling deprecated messageReceiveFailed method");
            backendConnector.messageReceiveFailed(messageId, event.getEndpoint());
        }
    }

    protected boolean isNewMessageReceiveFailureDefined(BackendConnector backendConnector) throws ClassNotFoundException {
        final Class<?> pluginImplementationClass = classUtil.getTargetObjectClass(backendConnector);
        boolean isNewMessageReceiveFailureDefined = true;
        try {
            pluginImplementationClass.getDeclaredMethod("messageReceiveFailed", MessageReceiveFailureEvent.class);
        } catch (NoSuchMethodException e) {
            LOG.debug("New messageReceiveFailed(MessageReceiveFailureEvent.class) is not defined");
            isNewMessageReceiveFailureDefined = false;
        }

        return isNewMessageReceiveFailureDefined;
    }
}
