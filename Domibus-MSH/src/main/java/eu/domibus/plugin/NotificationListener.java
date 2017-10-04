
package eu.domibus.plugin;

import javax.jms.Queue;

/**
 * @author Christian Koch, Stefan Mueller
 */
public interface NotificationListener {
    String getBackendName();

    Queue getBackendNotificationQueue();

    BackendConnector.Mode getMode();
}
