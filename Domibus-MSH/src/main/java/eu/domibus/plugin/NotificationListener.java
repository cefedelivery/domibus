
package eu.domibus.plugin;

import eu.domibus.common.NotificationType;

import javax.jms.Queue;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
public interface NotificationListener {
    String getBackendName();

    Queue getBackendNotificationQueue();

    BackendConnector.Mode getMode();

    List<NotificationType> getRequiredNotificationTypeList();
}
