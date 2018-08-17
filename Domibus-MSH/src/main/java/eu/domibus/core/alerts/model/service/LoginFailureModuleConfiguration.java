package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class LoginFailureModuleConfiguration implements AlertModuleConfiguration {

    private static final  Logger LOG = LoggerFactory.getLogger(LoginFailureModuleConfiguration.class);

    private final Boolean loginFailureActive;

    private AlertLevel loginFailureAlertLevel;

    private String loginFailureMailSubject;

    public LoginFailureModuleConfiguration() {
        this.loginFailureActive = false;
    }

    public LoginFailureModuleConfiguration(
            AlertLevel loginFailureAlertLevel,
            String loginFailureMailSubject) {
        this.loginFailureActive=true;
        this.loginFailureAlertLevel = loginFailureAlertLevel;
        this.loginFailureMailSubject=loginFailureMailSubject;
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
        final AlertType userLoginFailure = AlertType.USER_LOGIN_FAILURE;
        if(userLoginFailure !=alert.getAlertType()){
            LOG.error("Invalid alert type[{}] for this strategy, it should be[{}]",alert.getAlertType(), userLoginFailure);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        return loginFailureAlertLevel;
    }


}
