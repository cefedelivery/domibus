package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.ConsoleUserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserEntityBase;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserPersistenceService;
import eu.domibus.common.services.UserService;
import eu.domibus.security.ConsoleUserSecurityPolicyManager;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Service("userManagementService")
@Primary
public class UserManagementServiceImpl implements UserService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserManagementServiceImpl.class);

//    protected static final String LOGIN_SUSPENSION_TIME = "domibus.console.login.suspension.time";

    @Autowired
    protected UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    protected ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected UserConverter userConverter;

    @Autowired
    protected UserPersistenceService userPersistenceService;

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    protected DomainService domainService;

    @Autowired
    ConsoleUserSecurityPolicyManager userPasswordManager;

    @Autowired
    ConsoleUserAlertsServiceImpl userAlertsService;


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
        return userPasswordManager.handleWrongAuthentication(userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void reactivateSuspendedUsers() {
        userPasswordManager.reactivateSuspendedUsers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCorrectAuthentication(final String userName) {
        userPasswordManager.handleCorrectAuthentication(userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateExpiredPassword(final String userName) {
        UserEntityBase user = getUserWithName(userName);
        boolean defaultPassword = user.hasDefaultPassword();
        LocalDateTime passwordChangeDate = user.getPasswordChangeDate();

        userPasswordManager.validatePasswordExpired(userName, defaultPassword, passwordChangeDate);
    }

    @Override
    public Integer getDaysTillExpiration(String userName) {
        UserEntityBase user = getUserWithName(userName);
        boolean isDefaultPassword = user.hasDefaultPassword();
        LocalDateTime passwordChangeDate = user.getPasswordChangeDate();

        return userPasswordManager.getDaysTillExpiration(userName, isDefaultPassword, passwordChangeDate);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void triggerPasswordAlerts() {
        userAlertsService.triggerPasswordExpirationEvents();
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        userPersistenceService.changePassword(username, currentPassword, newPassword);
    }

    private UserEntityBase getUserWithName(String userName) {
        UserEntityBase user = userDao.findByUserName(userName);
        if (user == null) {
            throw new UserManagementException("Could not find console user with the name " + userName);
        }
        return user;
    }
}
