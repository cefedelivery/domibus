package eu.domibus.core.alerts.model.service;

import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class MessagingModuleConfiguration implements AlertModuleConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(MessagingModuleConfiguration.class);

    private final boolean messageCommunicationActive;

    private String mailSubject;

    private Map<MessageStatus, AlertLevel> messageStatusLevels = new HashMap<>();

    public MessagingModuleConfiguration(final String mailSubject) {
        this.messageCommunicationActive = true;
        this.mailSubject = mailSubject;

    }

    public MessagingModuleConfiguration() {
        this.messageCommunicationActive = false;

    }

    public void addStatusLevelAssociation(MessageStatus messageStatus, AlertLevel alertLevel) {
        messageStatusLevels.put(messageStatus, alertLevel);
    }

    public boolean shouldMonitorMessageStatus(MessageStatus messageStatus) {
        return messageCommunicationActive && messageStatusLevels.get(messageStatus) != null;
    }

    public AlertLevel getAlertLevel(MessageStatus messageStatus) {
        return messageStatusLevels.get(messageStatus);
    }

    @Override
    public String toString() {
        return "MessagingConfiguration{" +
                "messageCommunicationActive=" + messageCommunicationActive +
                ", messageStatusLevels=" + messageStatusLevels +
                '}';
    }

    @Override
    public String getMailSubject() {
        return mailSubject;
    }

    @Override
    public boolean isActive() {
        return messageCommunicationActive;
    }

    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        if (AlertType.MSG_COMMUNICATION_FAILURE != alert.getAlertType()) {
            LOG.error("Invalid alert type[{}] for this strategy, it should be[{}]", alert.getAlertType(), AlertType.MSG_COMMUNICATION_FAILURE);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        final StringPropertyValue stringPropertyValue = (StringPropertyValue) alert.getEvents().iterator().next().getProperties().get(MessageEvent.NEW_STATUS.name());
        final MessageStatus newStatus = MessageStatus.valueOf(stringPropertyValue.getValue());
        final AlertLevel alertLevel = getAlertLevel(newStatus);
        LOG.debug("Alert level for message change to status[{}] is [{}]", newStatus, alertLevel);
        return alertLevel;
    }

}
