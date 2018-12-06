package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserPersistenceService;
import eu.domibus.common.services.UserService;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.service.AlertEventModuleConfiguration;
import eu.domibus.core.alerts.model.service.LoginFailureModuleConfiguration;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.domibus.common.model.security.UserLoginErrorReason.BAD_CREDENTIALS;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Service("userManagementService")
@Primary
public class UserManagementServiceImpl implements UserService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserManagementServiceImpl.class);

    protected static final String MAXIMUM_LOGIN_ATTEMPT = "domibus.console.login.maximum.attempt";

    protected static final String LOGIN_SUSPENSION_TIME = "domibus.console.login.suspension.time";

    protected final static String MAXIMUM_PASSWORD_AGE = "domibus.passwordPolicy.expiration";

    protected final static String MAXIMUM_DEFAULT_PASSWORD_AGE = "domibus.passwordPolicy.defaultPasswordExpiration";

    protected final static String WARNING_DAYS_BEFORE_EXPIRATION = "domibus.passwordPolicy.warning.beforeExpiration";

    private static final String CREDENTIALS_EXPIRED = "Expired";

    @Autowired
    protected UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    protected UserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected UserConverter userConverter;

    @Autowired
    protected UserPersistenceService userPersistenceService;

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Autowired
    private EventService eventService;

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    protected DomainService domainService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<eu.domibus.api.user.User> findUsers() {
        List<User> userEntities = userDao.listUsers();
        List<eu.domibus.api.user.User> users = userConverter.convert(userEntities);

        String domainCode = domainContextProvider.getCurrentDomainSafely().getCode();
        users.forEach(u -> u.setDomain(domainCode));

        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<eu.domibus.api.user.UserRole> findUserRoles() {
        List<UserRole> userRolesEntities = userRoleDao.listRoles();

        List<eu.domibus.api.user.UserRole> userRoles = new ArrayList<>();
        for (UserRole userRoleEntity : userRolesEntities) {
            eu.domibus.api.user.UserRole userRole = new eu.domibus.api.user.UserRole(userRoleEntity.getName());
            userRoles.add(userRole);
        }
        return userRoles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateUsers(List<eu.domibus.api.user.User> users) {
        userPersistenceService.updateUsers(users);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserLoginErrorReason handleWrongAuthentication(final String userName) {
        User user = userDao.loadUserByUsername(userName);
        UserLoginErrorReason userLoginErrorReason = canApplyAccountLockingPolicy(userName, user);
        if (BAD_CREDENTIALS.equals(userLoginErrorReason)) {
            applyAccountLockingPolicy(user);
        }
        userDao.flush();
        triggerEvent(userName, userLoginErrorReason);
        return userLoginErrorReason;
    }

    protected void triggerEvent(String userName, UserLoginErrorReason userLoginErrorReason) {

        final LoginFailureModuleConfiguration loginFailureConfiguration = multiDomainAlertConfigurationService.getLoginFailureConfiguration();
        LOG.debug("loginFailureConfiguration.isActive() : [{}]", loginFailureConfiguration.isActive());
        switch (userLoginErrorReason) {
            case BAD_CREDENTIALS:
                if (loginFailureConfiguration.isActive()) {
                    eventService.enqueueLoginFailureEvent(userName, new Date(), false);
                }
                break;
            case UNKNOWN:
                break;
            case INACTIVE:
            case SUSPENDED:
                final AccountDisabledModuleConfiguration accountDisabledConfiguration = multiDomainAlertConfigurationService.getAccountDisabledConfiguration();
                if (accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin()) {
                    eventService.enqueueAccountDisabledEvent(userName, new Date(), true);
                } else if (loginFailureConfiguration.isActive()) {
                    eventService.enqueueLoginFailureEvent(userName, new Date(), true);
                }
                break;
        }
    }

    UserLoginErrorReason canApplyAccountLockingPolicy(String userName, User user) {

        if (user == null) {
            LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_UNKNOWN_USER, userName);
            return UserLoginErrorReason.UNKNOWN;
        }
        if (!user.isEnabled() && user.getSuspensionDate() == null) {
            LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_INACTIVE_USER, userName);
            return UserLoginErrorReason.INACTIVE;
        }
        if (!user.isEnabled() && user.getSuspensionDate() != null) {
            LOG.securityWarn(DomibusMessageCode.SEC_CONSOLE_LOGIN_SUSPENDED_USER, userName);
            return UserLoginErrorReason.SUSPENDED;
        }

        LOG.securityWarn(DomibusMessageCode.SEC_CONSOLE_LOGIN_BAD_CREDENTIALS, userName);
        return BAD_CREDENTIALS;
    }

    protected void applyAccountLockingPolicy(User user) {
        final Domain domain = getCurrentOrDefaultDomainForUser(user);

        int maxAttemptAmount = domibusPropertyProvider.getIntegerDomainProperty(domain, MAXIMUM_LOGIN_ATTEMPT);

        user.setAttemptCount(user.getAttemptCount() + 1);
        if (user.getAttemptCount() >= maxAttemptAmount) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Applying account locking policy, max number of attempt ([{}]) reached for user [{}]", maxAttemptAmount, user.getUserName());
            }
            user.setActive(false);
            final Date suspensionDate = new Date(System.currentTimeMillis());
            user.setSuspensionDate(suspensionDate);
            LOG.securityWarn(DomibusMessageCode.SEC_CONSOLE_LOGIN_LOCKED_USER, user.getUserName(), maxAttemptAmount);

            final AccountDisabledModuleConfiguration accountDisabledConfiguration = multiDomainAlertConfigurationService.getAccountDisabledConfiguration();
            if (accountDisabledConfiguration.isActive()) {
                eventService.enqueueAccountDisabledEvent(user.getUserName(), suspensionDate, true);
            }

        }
        userDao.update(user);
    }

    private Domain getCurrentOrDefaultDomainForUser(User user) {
        String domainCode;
        boolean isSuperAdmin = user.isSuperAdmin();
        if (isSuperAdmin) {
            domainCode = DomainService.DEFAULT_DOMAIN.getCode();
        } else {
            domainCode = userDomainService.getDomainForUser(user.getUserName());
        }
        return domainService.getDomain(domainCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void reactivateSuspendedUsers() {
        Domain domain = domainContextProvider.getCurrentDomainSafely();

        int suspensionInterval;
        if (domain == null) { //it is called for super-users so we read from default domain
            suspensionInterval = domibusPropertyProvider.getIntegerProperty(LOGIN_SUSPENSION_TIME);
        } else { //for normal users the domain is set as current Domain
            suspensionInterval = domibusPropertyProvider.getIntegerDomainProperty(LOGIN_SUSPENSION_TIME);
        }

        //user will not be reactivated.
        if (suspensionInterval <= 0) {
            return;
        }

        Date currentTimeMinusSuspensionInterval = new Date(System.currentTimeMillis() - (suspensionInterval * 1000));

        List<User> users = userDao.getSuspendedUsers(currentTimeMinusSuspensionInterval);
        for (User user : users) {
            LOG.debug("Suspended user [{}] is going to be reactivated.", user.getUserName());

            user.setSuspensionDate(null);
            user.setAttemptCount(0);
            user.setActive(true);
        }
        userDao.update(users);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCorrectAuthentication(final String userName) {
        User user = userDao.loadActiveUserByUsername(userName);
        LOG.debug("handleCorrectAuthentication for user [{}]", userName);
        if (user.getAttemptCount() > 0) {
            LOG.debug("user [{}] has [{}] attempt ", userName, user.getAttemptCount());
            LOG.debug("resetting to 0");
            user.setAttemptCount(0);
            userDao.update(user);
        }

        userDao.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateExpiredPassword(final String userName) {
        User user = userDao.loadActiveUserByUsername(userName);

        String expirationProperty = user.hasDefaultPassword() ? MAXIMUM_DEFAULT_PASSWORD_AGE : MAXIMUM_PASSWORD_AGE;
        int maxPasswordAgeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(expirationProperty));
        LOG.debug("Password expiration policy for user [{}] : {} days", userName, maxPasswordAgeInDays);

        if (maxPasswordAgeInDays == 0) {
            return;
        }

        LocalDate expirationDate = user.getPasswordChangeDate() == null ? LocalDate.now() :
                user.getPasswordChangeDate().plusDays(maxPasswordAgeInDays).toLocalDate();

        if (expirationDate.isBefore(LocalDate.now())) {
            LOG.debug("Password expired for user [{}]", user.getUserName());
            throw new CredentialsExpiredException(CREDENTIALS_EXPIRED);
        }
    }

    @Override
    public Integer getDaysTillExpiration(String userName) {

        int warningDaysBeforeExpiration = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(WARNING_DAYS_BEFORE_EXPIRATION));
        if (warningDaysBeforeExpiration == 0) {
            return null;
        }
        User user = userDao.loadActiveUserByUsername(userName);

        String expirationProperty = user.hasDefaultPassword() ? MAXIMUM_DEFAULT_PASSWORD_AGE : MAXIMUM_PASSWORD_AGE;
        int maxPasswordAgeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(expirationProperty));

        if (maxPasswordAgeInDays == 0) {
            return null;
        }

        if (warningDaysBeforeExpiration >= maxPasswordAgeInDays) {
            LOG.warn("Password policy: days until expiration for user [{}] is greater that max age.", userName);
            return null;
        }

        LocalDate passwordDate = user.getPasswordChangeDate().toLocalDate();
        if (passwordDate == null) {
            LOG.debug("Password policy: expiration date for user [{}] is not set", userName);
            return null;
        }

        LocalDate expirationDate = passwordDate.plusDays(maxPasswordAgeInDays);
        LocalDate today = LocalDate.now();
        int daysUntilExpiration = Period.between(today, expirationDate).getDays();

        LOG.debug("Password policy: days until expiration for user [{}] : {} days", userName, daysUntilExpiration);

        if (0 <= daysUntilExpiration && daysUntilExpiration <= warningDaysBeforeExpiration) {
            return daysUntilExpiration;
        } else {
            return null;
        }
    }

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

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        userPersistenceService.changePassword(username, currentPassword, newPassword);
    }

    protected void sendImminentExpirationAlerts(boolean usersWithDefaultPassword) {
        final AlertEventModuleConfiguration eventConfiguration = multiDomainAlertConfigurationService.getRepetitiveEventConfiguration(AlertType.PASSWORD_IMMINENT_EXPIRATION);
        if (!eventConfiguration.isActive()) {
            return;
        }

        final Integer duration = eventConfiguration.getEventDelay();
        String expirationProperty = usersWithDefaultPassword ? MAXIMUM_DEFAULT_PASSWORD_AGE : MAXIMUM_PASSWORD_AGE;
        int maxPasswordAgeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(expirationProperty));

        LocalDate from = LocalDate.now().minusDays(maxPasswordAgeInDays);
        LocalDate to = LocalDate.now().minusDays(maxPasswordAgeInDays).plusDays(duration);
        LOG.debug("ImminentExpirationAlerts: Searching for " + (usersWithDefaultPassword ? "default " : "") + "users with password change date between [{}]->[{}]", from, to);

        List<User> eligibleUsers = userDao.findWithPasswordChangedBetween(from, to, usersWithDefaultPassword);
        LOG.debug("ImminentExpirationAlerts: Found [{}] eligible " + (usersWithDefaultPassword ? "default " : "") + "users", eligibleUsers.size());

        eligibleUsers.forEach(user -> {
            eventService.enqueuePasswordImminentExpirationEvent(user, maxPasswordAgeInDays);
        });
    }

    protected void sendExpiredAlerts(boolean usersWithDefaultPassword) {
        final AlertEventModuleConfiguration eventConfiguration = multiDomainAlertConfigurationService.getRepetitiveEventConfiguration(AlertType.PASSWORD_EXPIRED);
        if (!eventConfiguration.isActive()) {
            return;
        }
        final Integer duration = eventConfiguration.getEventDelay();
        String expirationProperty = usersWithDefaultPassword ? MAXIMUM_DEFAULT_PASSWORD_AGE : MAXIMUM_PASSWORD_AGE;
        int maxPasswordAgeInDays = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(expirationProperty));

        LocalDate from = LocalDate.now().minusDays(maxPasswordAgeInDays).minusDays(duration);
        LocalDate to = LocalDate.now().minusDays(maxPasswordAgeInDays);
        LOG.debug("PasswordExpiredAlerts: Searching for " + (usersWithDefaultPassword ? "default " : "") + "users with password change date between [{}]->[{}]", from, to);

        List<User> eligibleUsers = userDao.findWithPasswordChangedBetween(from, to, usersWithDefaultPassword);
        LOG.debug("PasswordExpiredAlerts: Found [{}] eligible " + (usersWithDefaultPassword ? "default " : "") + "users", eligibleUsers.size());

        eligibleUsers.forEach(user -> {
            eventService.enqueuePasswordExpiredEvent(user, maxPasswordAgeInDays);
        });
    }

}
