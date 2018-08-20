package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserService;
import eu.domibus.core.alerts.model.service.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.service.LoginFailureModuleConfiguration;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static eu.domibus.common.model.security.UserLoginErrorReason.BAD_CREDENTIALS;


import eu.domibus.common.services.UserPersistenceService;
import org.springframework.context.annotation.Primary;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Service("userManagementService")
@Primary
public class UserManagementServiceImpl implements UserService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserManagementServiceImpl.class);

    protected static final String MAXIMUM_LOGIN_ATTEMPT = "domibus.console.login.maximum.attempt";

    protected final static String LOGIN_SUSPENSION_TIME = "domibus.console.login.suspension.time";

    private static final String DEFAULT_SUSPENSION_TIME = "3600";

    private static final String DEFAULT_LOGIN_ATTEMPT = "5";

    @Autowired
    protected UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

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
        int maxAttemptAmount;
        try {
            final Domain domain = getCurrentOrDefaultDomainForUser(user);

            String maxAttemptAmountPropVal = domibusPropertyProvider.getDomainProperty(domain, MAXIMUM_LOGIN_ATTEMPT, DEFAULT_LOGIN_ATTEMPT);
            maxAttemptAmount = Integer.valueOf(maxAttemptAmountPropVal);
        } catch (NumberFormatException n) {
            maxAttemptAmount = Integer.valueOf(DEFAULT_LOGIN_ATTEMPT);
        }
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
        boolean isSuperAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals(AuthRole.ROLE_AP_ADMIN.name()));
        if (isSuperAdmin) {
            domainCode = domainService.DEFAULT_DOMAIN.getCode();
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
        int suspensionInterval;

        String suspensionIntervalPropValue;
        if(domainContextProvider.getCurrentDomainSafely() == null) { //it is called for super-users so we read from default domain
            suspensionIntervalPropValue = domibusPropertyProvider.getProperty(LOGIN_SUSPENSION_TIME, DEFAULT_SUSPENSION_TIME);
        } else { //for normal users the domain is set as current Domain
            suspensionIntervalPropValue = domibusPropertyProvider.getDomainProperty(LOGIN_SUSPENSION_TIME, DEFAULT_SUSPENSION_TIME);
        }
        try {
            suspensionInterval = Integer.valueOf(suspensionIntervalPropValue);
        } catch (NumberFormatException n) {
            suspensionInterval = Integer.valueOf(DEFAULT_SUSPENSION_TIME);
        }
        //user will not be reactivated.
        if (suspensionInterval == 0) {
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

}
