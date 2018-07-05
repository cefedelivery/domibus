package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginFailureConfiguration implements AlertConfiguration{

    private final static Logger LOG = LoggerFactory.getLogger(LoginFailureConfiguration.class);

    private final Boolean loginFailureActive;
    private AlertLevel loginFailureAlertLevel;
    private String loginFailureMailSubject;

    public LoginFailureConfiguration(boolean loginFailureActive) {
        this.loginFailureActive = loginFailureActive;
    }

    public LoginFailureConfiguration(
            Boolean loginFailureActive,
            AlertLevel loginFailureAlertLevel,
            String loginFailureMailSubject) {
        this(loginFailureActive);
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
