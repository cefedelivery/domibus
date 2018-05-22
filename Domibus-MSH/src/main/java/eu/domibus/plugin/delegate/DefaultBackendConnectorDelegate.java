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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Notifying connector [{}] about status change event [{}]", backendConnector.getName(), event);
        }
        backendConnector.messageStatusChanged(event);
    }

    @Override
    public void messageReceiveFailed(BackendConnector backendConnector, MessageReceiveFailureEvent event) {
        LOG.info("Calling messageReceiveFailed method");
        backendConnector.messageReceiveFailed(event);
    }

}
