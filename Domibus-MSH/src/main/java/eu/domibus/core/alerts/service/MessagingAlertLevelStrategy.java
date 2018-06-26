package eu.domibus.core.alerts.service;

import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.MessageEvent;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.EventPropertyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Strategy to determine the type of alert level should be processed for a messaging related alert.
 * In this scenario the alert level is linked to the message status that triggered the alert.
 */
@Component
public class MessagingAlertLevelStrategy implements AlertLevelStrategy {

    private final static Logger LOG = LoggerFactory.getLogger(MessagingAlertLevelStrategy.class);

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    /**
     * Retrieve the new message status from the alert. Then retrieve the alert level linked for this status based on
     * the domibus configuration.
     *
     * @param alert the messaging alert.
     * @return
     */
    @Override
    public AlertLevel getAlertLevel(final Alert alert) {
        if(AlertType.MSG_COMMUNICATION_FAILURE!=alert.getAlertType()){
            LOG.error("Invalid alert type[{}] for this strategy, it should be[{}]",alert.getAlertType(),AlertType.MSG_COMMUNICATION_FAILURE);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        final EventPropertyValue eventPropertyValue = alert.getEvents().iterator().next().getProperties().get(MessageEvent.NEW_STATUS.name());
        final MessageStatus newStatus = MessageStatus.valueOf(eventPropertyValue.getValue());
        final AlertLevel alertLevel = multiDomainAlertConfigurationService.getMessageCommunicationConfiguration().getAlertLevel(newStatus);
        LOG.debug("Alert level for message change to status[{}] is [{}]", newStatus, alertLevel);
        return alertLevel;
    }

}
