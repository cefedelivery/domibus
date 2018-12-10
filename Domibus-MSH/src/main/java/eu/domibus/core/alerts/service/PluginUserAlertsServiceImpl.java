package eu.domibus.core.alerts.service;

import eu.domibus.common.dao.security.UserDaoBase;
import eu.domibus.common.model.security.UserBase;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.LoginFailureModuleConfiguration;
import eu.domibus.core.security.AuthenticationDAO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Service
public class PluginUserAlertsServiceImpl extends UserAlertsServiceImpl {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginUserAlertsServiceImpl.class);

    protected final static String MAXIMUM_PASSWORD_AGE = "domibus.plugin_passwordPolicy.expiration";
    protected final static String MAXIMUM_DEFAULT_PASSWORD_AGE = "domibus.plugin_passwordPolicy.defaultPasswordExpiration";

    @Autowired
    protected AuthenticationDAO userDao;

    @Autowired
    private MultiDomainAlertConfigurationService alertConfiguration;

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
    protected AlertType getAlertTypeForPasswordImminentExpiration() { return AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION; }

    @Override
    protected AlertType getAlertTypeForPasswordExpired() {
        return AlertType.PLUGIN_PASSWORD_EXPIRED;
    }

    @Override
    protected EventType getEventTypeForPasswordImminentExpiration() {
        return EventType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    protected EventType getEventTypeForPasswordExpired() {
        return EventType.PLUGIN_PASSWORD_EXPIRED;
    }

    @Override
    protected UserDaoBase getUserDao() { return userDao; }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // TODO: move this code to base class to use the same code for console and plugin users
    public void triggerLoginFailureEvent(UserBase user) {
        final LoginFailureModuleConfiguration configuration = alertConfiguration.getPluginLoginFailureConfiguration();
        LOG.debug("Plugin login Failure Configuration active() : [{}]", configuration.isActive());

        if (configuration.isActive()) {
            eventService.enqueuePluginLoginFailureEvent(user.getUserName(), new Date());
        }
    }
}
