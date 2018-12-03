package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.security.IUser;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.AlertEventModuleConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Autowired
    private EventService eventService;

    protected abstract String getMaximumDefaultPasswordAgeProperty();

    protected abstract String getMaximumPasswordAgeProperty();

    protected abstract List<IUser> getUsersWithPasswordChangedBetween(boolean usersWithDefaultPassword, LocalDate from, LocalDate to);

    protected abstract AlertType getAlertTypeForPasswordImminentExpiration();

    protected abstract AlertType getAlertTypeForPasswordExpired();

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAlerts() {
        try {
            sendExpiredAlerts(true);
            sendExpiredAlerts(false);
        } catch (Exception ex) {
            LOG.error("Send password expired alerts failed ", ex);
        }
        try {
            sendImminentExpirationAlerts(true);
            sendImminentExpirationAlerts(false);
        } catch (Exception ex) {
            LOG.error("Send imminent expiration alerts failed ", ex);
        }
    }

    protected void sendImminentExpirationAlerts(boolean usersWithDefaultPassword) {
        final AlertEventModuleConfiguration eventConfiguration = multiDomainAlertConfigurationService.getRepetitiveEventConfiguration(getAlertTypeForPasswordImminentExpiration());
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

        eligibleUsers.forEach(user -> {
            eventService.enqueuePasswordImminentExpirationEvent(user, maxPasswordAgeInDays);
        });
    }

    protected void sendExpiredAlerts(boolean usersWithDefaultPassword) {
        final AlertEventModuleConfiguration eventConfiguration = multiDomainAlertConfigurationService.getRepetitiveEventConfiguration(getAlertTypeForPasswordExpired());
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

        eligibleUsers.forEach(user -> {
            eventService.enqueuePasswordExpiredEvent(user, maxPasswordAgeInDays);
        });
    }

}
