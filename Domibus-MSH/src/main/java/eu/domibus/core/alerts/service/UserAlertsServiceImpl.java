package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.security.IUser;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.AlertEventModuleConfiguration;
import eu.domibus.core.alerts.model.service.LoginFailureModuleConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Service
public abstract class UserAlertsServiceImpl implements UserAlertsService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserAlertsServiceImpl.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private MultiDomainAlertConfigurationService alertConfiguration;

    @Autowired
    private EventService eventService;

    protected abstract String getMaximumDefaultPasswordAgeProperty();

    protected abstract String getMaximumPasswordAgeProperty();

    protected abstract List<IUser> getUsersWithPasswordChangedBetween(boolean usersWithDefaultPassword, LocalDate from, LocalDate to);

    protected abstract AlertType getAlertTypeForPasswordImminentExpiration();

    protected abstract AlertType getAlertTypeForPasswordExpired();

    protected abstract EventType getEventTypeForPasswordImminentExpiration();

    protected abstract EventType getEventTypeForPasswordExpired();

    public void triggerLoginFailureEvent(String userName) {

        final LoginFailureModuleConfiguration configuration = alertConfiguration.getPluginLoginFailureConfiguration();
        LOG.debug("Plugin login Failure Configuration isActive() : [{}]", configuration.isActive());

        if (configuration.isActive()) {
            eventService.enqueuePluginLoginFailureEvent(userName, new Date());
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void triggetPasswordExpirationEvents() {
        try {
            triggerExpiredEvents(true);
            triggerExpiredEvents(false);
        } catch (Exception ex) {
            LOG.error("Send password expired alerts failed ", ex);
        }
        try {
            triggerImminentExpirationEvents(true);
            triggerImminentExpirationEvents(false);
        } catch (Exception ex) {
            LOG.error("Send imminent expiration alerts failed ", ex);
        }
    }

    void triggerImminentExpirationEvents(boolean usersWithDefaultPassword) {
        final AlertEventModuleConfiguration eventConfiguration = alertConfiguration.getRepetitiveEventConfiguration(getAlertTypeForPasswordImminentExpiration());
        if (!eventConfiguration.isActive()) {
            return;
        }

        final Integer duration = eventConfiguration.getEventDelay();
        String expirationProperty = usersWithDefaultPassword ? getMaximumDefaultPasswordAgeProperty() : getMaximumPasswordAgeProperty();
        int maxPasswordAgeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(expirationProperty));

        LocalDate from = LocalDate.now().minusDays(maxPasswordAgeInDays);
        LocalDate to = LocalDate.now().minusDays(maxPasswordAgeInDays).plusDays(duration);
        LOG.debug("ImminentExpirationAlerts: Searching for " + (usersWithDefaultPassword ? "default " : "") + "users with password change date between [{}]->[{}]", from, to);

        List<IUser> eligibleUsers = getUsersWithPasswordChangedBetween(usersWithDefaultPassword, from, to);
        LOG.debug("ImminentExpirationAlerts: Found [{}] eligible " + (usersWithDefaultPassword ? "default " : "") + "users", eligibleUsers.size());

        EventType eventType = getEventTypeForPasswordImminentExpiration();
        eligibleUsers.forEach(user -> {
            eventService.enqueuePasswordImminentExpirationEvent(eventType, user, maxPasswordAgeInDays);
        });
    }

    void triggerExpiredEvents(boolean usersWithDefaultPassword) {
        final AlertEventModuleConfiguration eventConfiguration = alertConfiguration.getRepetitiveEventConfiguration(getAlertTypeForPasswordExpired());
        if (!eventConfiguration.isActive()) {
            return;
        }
        final Integer duration = eventConfiguration.getEventDelay();
        String expirationProperty = usersWithDefaultPassword ? getMaximumDefaultPasswordAgeProperty() : getMaximumPasswordAgeProperty();
        int maxPasswordAgeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(expirationProperty));

        LocalDate from = LocalDate.now().minusDays(maxPasswordAgeInDays).minusDays(duration);
        LocalDate to = LocalDate.now().minusDays(maxPasswordAgeInDays);
        LOG.debug("PasswordExpiredAlerts: Searching for " + (usersWithDefaultPassword ? "default " : "") + "users with password change date between [{}]->[{}]", from, to);

        List<IUser> eligibleUsers = getUsersWithPasswordChangedBetween(usersWithDefaultPassword, from, to);
        LOG.debug("PasswordExpiredAlerts: Found [{}] eligible " + (usersWithDefaultPassword ? "default " : "") + "users", eligibleUsers.size());

        EventType eventType = getEventTypeForPasswordExpired();
        eligibleUsers.forEach(user -> {
            eventService.enqueuePasswordExpiredEvent(eventType, user, maxPasswordAgeInDays);
        });
    }

}
