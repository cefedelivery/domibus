package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatorConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticatorConfiguration.class);

    final Boolean loginFailureActive;
    final Boolean accountDisabledActive;
    AlertLevel loginFailureAlertLevel;
    AlertLevel accountDisabledAlertLevel;
    AccountDisabledMoment accountDisabledMoment;

    public AuthenticatorConfiguration(boolean loginFailureActive, boolean accountDisabledActive) {
        this.loginFailureActive = loginFailureActive;
        this.accountDisabledActive = accountDisabledActive;
    }

    public AuthenticatorConfiguration(Boolean loginFailureActive, Boolean accountDisabledActive, AlertLevel loginFailureAlertLevel, AlertLevel accountDisabledAlertLevel, AccountDisabledMoment accountDisabledMoment) {
        this(loginFailureActive, accountDisabledActive);
        this.loginFailureAlertLevel = loginFailureAlertLevel;
        this.accountDisabledAlertLevel = accountDisabledAlertLevel;
        this.accountDisabledMoment = accountDisabledMoment;
    }

    public Boolean getLoginFailureActive() {
        return loginFailureActive;
    }

    public Boolean getAccountDisabledActive() {
        return accountDisabledActive;
    }

    public AlertLevel getLoginFailureAlertLevel() {
        return loginFailureAlertLevel;
    }

    public AlertLevel getAccountDisabledAlertLevel() {
        return accountDisabledAlertLevel;
    }

    public AccountDisabledMoment getAccountDisabledMoment() {
        return accountDisabledMoment;
    }
}
