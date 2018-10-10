package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class AlertEventModuleConfiguration implements AlertModuleConfiguration {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertEventModuleConfiguration.class);

    private final Boolean eventActive;
    private Integer eventDelay;
    private Integer eventFrequency;
    private AlertLevel eventAlertLevel;
    private String eventMailSubject;

    AlertType alertType;

    public AlertEventModuleConfiguration(AlertType alertType) {
        this.eventActive = false;

        this.alertType = alertType;
    }

    public AlertEventModuleConfiguration(AlertType alertType, Integer eventDelay, Integer eventFrequency, AlertLevel eventAlertLevel, String eventMailSubject) {
        this.eventActive = true;

        this.alertType = alertType;

        this.eventDelay = eventDelay;
        this.eventFrequency = eventFrequency;
        this.eventAlertLevel = eventAlertLevel;
        this.eventMailSubject = eventMailSubject;
    }
    
    public Integer getEventDelay() {
        return eventDelay;
    }

    public Integer getEventFrequency() { return eventFrequency; }

    @Override
    public String getMailSubject() {
        return eventMailSubject;
    }

    @Override
    public boolean isActive() {
        return eventActive;
    }

    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        if (alertType != alert.getAlertType()) {
            LOG.error("Invalid alert type[{}] for this strategy, it should be[{}]", alert.getAlertType(), this.alertType);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        return eventAlertLevel;
    }


}
