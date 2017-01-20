package eu.domibus.plugin.delegate;

import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.plugin.BackendConnector;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public interface BackendConnectorDelegate {

    void messageReceiveFailed(BackendConnector backendConnector, MessageReceiveFailureEvent event);
}
