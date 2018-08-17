package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AccountDisabledModuleConfiguration implements AlertModuleConfiguration {

    private static final  Logger LOG = LoggerFactory.getLogger(AccountDisabledModuleConfiguration.class);

    private final Boolean accountDisabledActive;

    private AlertLevel accountDisabledAlertLevel;

    private AccountDisabledMoment accountDisabledMoment;

    private String accountDisabledMailSubject;

    public AccountDisabledModuleConfiguration() {
        this.accountDisabledActive = false;
    }

    public AccountDisabledModuleConfiguration(
            AlertLevel accountDisabledAlertLevel,
            AccountDisabledMoment accountDisabledMoment,
            String accountDisabledMailSubject) {
        this.accountDisabledActive = true;
        this.accountDisabledAlertLevel = accountDisabledAlertLevel;
        this.accountDisabledMoment = accountDisabledMoment;
        this.accountDisabledMailSubject=accountDisabledMailSubject;
    }


    public Boolean shouldTriggerAccountDisabledAtEachLogin(){
        return accountDisabledActive && accountDisabledMoment == AccountDisabledMoment.AT_LOGON;
    }


    @Override
    public String getMailSubject() {
        return accountDisabledMailSubject;
    }

    @Override
    public boolean isActive() {
        return accountDisabledActive;
    }

    @Override
    public AlertLevel getAlertLevel(Alert alert) {
        final AlertType userAccountDisabled = AlertType.USER_ACCOUNT_DISABLED;
        if(userAccountDisabled !=alert.getAlertType()){
            LOG.error("Invalid alert type[{}] for this strategy, it should be[{}]",alert.getAlertType(), userAccountDisabled);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        return accountDisabledAlertLevel;
    }

}
