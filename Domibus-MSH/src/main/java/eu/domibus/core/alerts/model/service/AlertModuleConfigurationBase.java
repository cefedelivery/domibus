package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class AlertModuleConfigurationBase implements AlertModuleConfiguration {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertModuleConfigurationBase.class);

    AlertType alertType;
    protected boolean isActive;
    protected AlertLevel alertLevel;
    protected String mailSubject;

    private AlertModuleConfigurationBase(AlertType alertType, boolean isActive) {
        this.alertType = alertType;
        this.isActive = isActive;
    }

    public AlertModuleConfigurationBase(AlertType alertType) {
        this(alertType, false);
    }

    public AlertModuleConfigurationBase(AlertType alertType, AlertLevel alertLevel, String emailSubject) {
        this(alertType, true);

        this.alertLevel = alertLevel;
        this.mailSubject = emailSubject;
    }

    public AlertModuleConfigurationBase(AlertType alertType, String emailSubject) {
        this(alertType, null, emailSubject);
    }

    @Override
    public String getMailSubject() {
        return mailSubject;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        if (alertType != alert.getAlertType()) {
            LOG.error("Invalid alert type[{}] for this strategy, it should be[{}]", alert.getAlertType(), alertType);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        return alertLevel;
    }

}
