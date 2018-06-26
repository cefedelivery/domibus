package eu.domibus.core.alerts.service;

import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.MessageEvent;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.EventPropertyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessagingAlertLevelStrategy implements AlertLevelStrategy {

    private final static Logger LOG = LoggerFactory.getLogger(MessagingAlertLevelStrategy.class);

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Override
    public AlertLevel getAlertLevel(final Event event) {
        final EventPropertyValue eventPropertyValue = event.getProperties().get(MessageEvent.NEW_STATUS.name());
        final MessageStatus newStatus = MessageStatus.valueOf(eventPropertyValue.getValue());
        final AlertLevel alertLevel = multiDomainAlertConfigurationService.getMessageCommunicationConfiguration().getAlertLevel(newStatus);
        LOG.debug("Alert level for message change to status[{}] is [{}]", newStatus, alertLevel);
        return alertLevel;
    }

}
