package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AccountDisabledModuleConfiguration extends AlertModuleConfigurationBase {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AccountDisabledModuleConfiguration.class);

    private AccountDisabledMoment accountDisabledMoment;

    public AccountDisabledModuleConfiguration() {
        super(AlertType.USER_ACCOUNT_DISABLED);
    }

    public AccountDisabledModuleConfiguration(AlertLevel alertLevel, AccountDisabledMoment moment, String mailSubject) {
        super(AlertType.USER_ACCOUNT_DISABLED, alertLevel, mailSubject);
        this.accountDisabledMoment = moment;
    }


    public Boolean shouldTriggerAccountDisabledAtEachLogin() {
        return isActive() && accountDisabledMoment == AccountDisabledMoment.AT_LOGON;
    }

}
