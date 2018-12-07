package eu.domibus.core.alerts.service;

import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.IUser;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Service
public class ConsoleUserAlertsServiceImpl extends UserAlertsServiceImpl {

    public final static String MAXIMUM_PASSWORD_AGE = "domibus.passwordPolicy.expiration";
    public final static String MAXIMUM_DEFAULT_PASSWORD_AGE = "domibus.passwordPolicy.defaultPasswordExpiration";

    @Autowired
    protected UserDao userDao;

    @Override
    protected String getMaximumDefaultPasswordAgeProperty() {
        return MAXIMUM_DEFAULT_PASSWORD_AGE;
    }

    @Override
    protected String getMaximumPasswordAgeProperty() {
        return MAXIMUM_PASSWORD_AGE;
    }

    @Override
    protected List<IUser> getUsersWithPasswordChangedBetween(boolean usersWithDefaultPassword, LocalDate from, LocalDate to) {
        return userDao.findWithPasswordChangedBetween(from, to, usersWithDefaultPassword);
    }

    @Override
    protected AlertType getAlertTypeForPasswordImminentExpiration() {
        return AlertType.PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    protected AlertType getAlertTypeForPasswordExpired() { return AlertType.PASSWORD_EXPIRED; }

}
