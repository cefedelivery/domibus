package eu.domibus.core.alerts.model.service;

import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.MessageEvent;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class MessagingModuleConfiguration extends AlertModuleConfigurationBase {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(MessagingModuleConfiguration.class);

    private Map<MessageStatus, AlertLevel> messageStatusLevels = new EnumMap<>(MessageStatus.class);

    public MessagingModuleConfiguration(final String mailSubject) {
        super(AlertType.MSG_STATUS_CHANGED, mailSubject);
    }

    public MessagingModuleConfiguration() {
        super(AlertType.MSG_STATUS_CHANGED);
    }

    public void addStatusLevelAssociation(MessageStatus messageStatus, AlertLevel alertLevel) {
        messageStatusLevels.put(messageStatus, alertLevel);
    }

    public boolean shouldMonitorMessageStatus(MessageStatus messageStatus) {
        return isActive && messageStatusLevels.get(messageStatus) != null;
    }

    public AlertLevel getAlertLevel(MessageStatus messageStatus) {
        return messageStatusLevels.get(messageStatus);
    }

    @Override
    public String toString() {
        return "MessagingConfiguration{" +
                "messageCommunicationActive=" + isActive +
                ", messageStatusLevels=" + messageStatusLevels +
                '}';
    }

    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        super.getAlertLevel(alert);

        final Event event = alert.getEvents().iterator().next();
        final Optional<String> stringPropertyValue = event.findStringProperty(MessageEvent.NEW_STATUS.name());
        final MessageStatus newStatus = MessageStatus.valueOf(stringPropertyValue.
                orElseThrow(() -> new IllegalArgumentException("New status property should always be present")));
        final AlertLevel alertLevel = getAlertLevel(newStatus);
        LOG.debug("Alert level for message change to status[{}] is [{}]", newStatus, alertLevel);
        return alertLevel;
    }

}
