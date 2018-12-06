package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class LoginFailureModuleConfiguration implements AlertModuleConfiguration {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(LoginFailureModuleConfiguration.class);

    private AlertType alertType;

    private final Boolean loginFailureActive;

    private AlertLevel loginFailureAlertLevel;

    private String loginFailureMailSubject;

    public LoginFailureModuleConfiguration() {
        this.loginFailureActive = false;
    }

    public LoginFailureModuleConfiguration(
            AlertType alertType,
            AlertLevel loginFailureAlertLevel,
            String loginFailureMailSubject) {
        this.alertType = alertType;
        this.loginFailureActive = true;
        this.loginFailureAlertLevel = loginFailureAlertLevel;
        this.loginFailureMailSubject = loginFailureMailSubject;
    }


    @Override
    public String getMailSubject() {
        return loginFailureMailSubject;
    }

    @Override
    public boolean isActive() {
        return loginFailureActive;
    }

    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        if (alertType != alert.getAlertType()) {
            LOG.error("Invalid alert type[{}] for this strategy, it should be[{}]", alert.getAlertType(), alertType);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        return loginFailureAlertLevel;
    }


}
