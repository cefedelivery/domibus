package eu.domibus.core.alerts.service;

import eu.domibus.api.user.UserBase;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserDaoBase;
import eu.domibus.common.model.security.UserEntityBase;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.service.LoginFailureModuleConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Service
public class ConsoleUserAlertsServiceImpl extends UserAlertsServiceImpl {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserAlertsServiceImpl.class);

    public final static String MAXIMUM_PASSWORD_AGE = "domibus.passwordPolicy.expiration";
    public final static String MAXIMUM_DEFAULT_PASSWORD_AGE = "domibus.passwordPolicy.defaultPasswordExpiration";

    @Autowired
    protected UserDao userDao;

    @Autowired
    private MultiDomainAlertConfigurationService alertsConfiguration;

    @Autowired
    private EventService eventService;

    @Override
    protected String getMaximumDefaultPasswordAgeProperty() {
        return MAXIMUM_DEFAULT_PASSWORD_AGE;
    }

    @Override
    protected String getMaximumPasswordAgeProperty() {
        return MAXIMUM_PASSWORD_AGE;
    }

    @Override
    protected AlertType getAlertTypeForPasswordImminentExpiration() {
        return AlertType.PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    protected AlertType getAlertTypeForPasswordExpired() { return AlertType.PASSWORD_EXPIRED; }

    @Override
    protected EventType getEventTypeForPasswordImminentExpiration() {
        return EventType.PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    protected EventType getEventTypeForPasswordExpired() {
        return EventType.PASSWORD_EXPIRED;
    }

    @Override
    protected UserDaoBase getUserDao() { return userDao; }

    @Override
    protected UserEntityBase.Type getUserType() {
        return UserEntityBase.Type.CONSOLE;
    }

    @Override
    protected AccountDisabledModuleConfiguration getAccountDisabledConfiguration() {
        return alertsConfiguration.getAccountDisabledConfiguration();
    }

    @Override
    protected LoginFailureModuleConfiguration getLoginFailureConfiguration() {
        return alertsConfiguration.getLoginFailureConfiguration();
    }

}
