
package eu.domibus.plugin;

import javax.jms.Queue;

/**
 * @author Christian Koch, Stefan Mueller
 */
public interface NotificationListener {
    public String getBackendName();

    public Queue getBackendNotificationQueue();
}
