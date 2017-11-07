package eu.domibus.common.services.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import eu.domibus.api.user.UserState;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Service
public class UserManagementServiceImpl implements UserService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserManagementServiceImpl.class);

    static final String MAXIMUM_LOGGIN_ATTEMPT = "domibus.console.login.maximum.attempt";

    static final String LOGIN_SUSPENSION_TIME = "domibus.console.login.suspension.time";

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Override
    @Transactional(readOnly = true)
    public List<eu.domibus.api.user.User> findUsers() {
        //@thom use a dozer custom mapper to map from role to authorities.
        List<User> userEntities = userDao.listUsers();
        List<eu.domibus.api.user.User> users = new ArrayList<>();
        for (User userEntity : userEntities) {
            List<String> authorities = new ArrayList<>();
            Collection<UserRole> roles = userEntity.getRoles();
            for (UserRole role : roles) {
                authorities.add(role.getName());
            }
            eu.domibus.api.user.User user = new eu.domibus.api.user.User(
                    userEntity.getUserName(),
                    userEntity.getEmail(),
                    userEntity.getActive(),
                    authorities,
                    UserState.PERSISTED, userEntity.getSuspensionDate());
            users.add(user);
        }
        return users;
    }

    @Override
    @Transactional
    public void saveUsers(List<eu.domibus.api.user.User> users) {
        Collection<eu.domibus.api.user.User> newUsers = filterNewUsers(users);
        LOG.debug("New users:" + newUsers.size());
        insertNewUsers(newUsers);
        Collection<eu.domibus.api.user.User> noPasswordChangedModifiedUsers = filterModifiedUserWithoutPasswordChange(users);
        LOG.debug("Modified users without password change:" + noPasswordChangedModifiedUsers.size());
        updateUserWithoutPasswordChange(noPasswordChangedModifiedUsers);
        Collection<eu.domibus.api.user.User> passwordChangedModifiedUsers = filterModifiedUserWithPasswordChange(users);
        LOG.debug("Modified users with password change:" + passwordChangedModifiedUsers.size());
        updateUserWithPasswordChange(passwordChangedModifiedUsers);
    }

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

    @Override
    public void updateUsers(List<eu.domibus.api.user.User> users) {
        // update
        Collection<eu.domibus.api.user.User> noPasswordChangedModifiedUsers = filterModifiedUserWithoutPasswordChange(users);
        LOG.debug("Modified users without password change:" + noPasswordChangedModifiedUsers.size());
        updateUserWithoutPasswordChange(noPasswordChangedModifiedUsers);
        Collection<eu.domibus.api.user.User> passwordChangedModifiedUsers = filterModifiedUserWithPasswordChange(users);
        LOG.debug("Modified users with password change:" + passwordChangedModifiedUsers.size());
        updateUserWithPasswordChange(passwordChangedModifiedUsers);

        // insertion
        Collection<eu.domibus.api.user.User> newUsers = filterNewUsers(users);
        LOG.debug("New users:" + newUsers.size());
        insertNewUsers(newUsers);

        // deletion
        List<User> usersEntities = domainConverter.convert(users, User.class);
        List<User> allUsersEntities = userDao.listUsers();
        List<User> usersEntitiesToDelete = usersToDelete(allUsersEntities, usersEntities);
        userDao.deleteAll(usersEntitiesToDelete);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void handleAuthenticationPolicy(final String userName) {
        User user = userDao.loadUserByUsername(userName);
        if (logOnly(userName, user)) {
            return;
        }
        applyAccountLockingPolicy(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void undoUserSuspension() {
        int suspensionInterval;
        try {
            suspensionInterval = Integer.valueOf(domibusProperties.getProperty(LOGIN_SUSPENSION_TIME, "3600"));
        } catch (NumberFormatException n) {
            suspensionInterval = 3600;
        }
        //user will not be reactivated.
        if (suspensionInterval == 0) {
            return;
        }

        Date currentTimeMinusSuspensionInterval = new Date(System.currentTimeMillis() - suspensionInterval * 1000);
        List<User> users = userDao.listSuspendedUser(currentTimeMinusSuspensionInterval);
        for (User user : users) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Suspended user " + user.getUserName() + " is going to reactivated.");
            }
            user.setSuspensionDate(null);
            user.setAttemptCount(0);
            user.setActive(true);
        }
        userDao.update(users);
    }

    void applyAccountLockingPolicy(User user) {
        int maxAttemptAmount;
        try {
            maxAttemptAmount = Integer.valueOf(domibusProperties.getProperty(MAXIMUM_LOGGIN_ATTEMPT, "5"));
        } catch (NumberFormatException n) {
            maxAttemptAmount = 5;
        }
        user.setAttemptCount(user.getAttemptCount() + 1);
        if (user.getAttemptCount() >= maxAttemptAmount) {
            user.setActive(false);
            user.setSuspensionDate(new Date(System.currentTimeMillis()));
        }
        userDao.update(user);
    }

    boolean logOnly(String userName, User user) {
        if (user == null) {
            LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_UNKNOWN_USER, userName);
            return true;
        }
        if (!user.isEnabled() && user.getSuspensionDate() == null) {
            LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_INACTIVE_USER, userName);
            return true;
        }
        if (!user.isEnabled() && user.getSuspensionDate() != null) {
            LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_SUSPENDED_USER, userName);
            return true;
        }
        return false;
    }

    private List<User> usersToDelete(final List<User> masterData, final List<User> newData) {
        List<User> result = new ArrayList<>(masterData);
        result.removeAll(newData);
        return result;
    }

    private void insertNewUsers(Collection<eu.domibus.api.user.User> users) {
        for (eu.domibus.api.user.User user : users) {
            List<String> authorities = user.getAuthorities();
            User userEntity = domainConverter.convert(user, User.class);
            addRoleToUser(authorities, userEntity);
            userEntity.setPassword(bcryptEncoder.encode(userEntity.getPassword()));
            userDao.create(userEntity);
        }
    }

    private void addRoleToUser(List<String> authorities, User userEntity) {
        for (String authority : authorities) {
            UserRole userRole = userRoleDao.findByName(authority);
            userEntity.addRole(userRole);
        }
    }

    private void updateUserWithoutPasswordChange(Collection<eu.domibus.api.user.User> users) {
        for (eu.domibus.api.user.User user : users) {
            User userEntity = prepareUserForUpdate(user);
            addRoleToUser(user.getAuthorities(), userEntity);
            userDao.update(userEntity);
        }
    }

    private void updateUserWithPasswordChange(Collection<eu.domibus.api.user.User> users) {
        for (eu.domibus.api.user.User user : users) {
            User userEntity = prepareUserForUpdate(user);
            userEntity.setPassword(bcryptEncoder.encode(user.getPassword()));
            addRoleToUser(user.getAuthorities(), userEntity);
            userDao.update(userEntity);
        }
    }

    User prepareUserForUpdate(eu.domibus.api.user.User user) {
        User userEntity = userDao.loadUserByUsername(user.getUserName());
        if (!userEntity.getActive() && user.isActive()) {
            userEntity.setSuspensionDate(null);
            userEntity.setAttemptCount(0);
        }
        userEntity.setActive(user.isActive());
        userEntity.setEmail(user.getEmail());
        userEntity.clearRoles();
        return userEntity;
    }

    private Collection<eu.domibus.api.user.User> filterNewUsers(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, new Predicate<eu.domibus.api.user.User>() {
            @Override
            public boolean apply(eu.domibus.api.user.User user) {
                return UserState.NEW.name().equals(user.getStatus());
            }
        });
    }

    private Collection<eu.domibus.api.user.User> filterModifiedUserWithoutPasswordChange(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, new Predicate<eu.domibus.api.user.User>() {
            @Override
            public boolean apply(eu.domibus.api.user.User user) {
                return UserState.UPDATED.name().equals(user.getStatus()) && user.getPassword() == null;
            }
        });
    }

    private Collection<eu.domibus.api.user.User> filterModifiedUserWithPasswordChange(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, new Predicate<eu.domibus.api.user.User>() {
            @Override
            public boolean apply(eu.domibus.api.user.User user) {
                return UserState.UPDATED.name().equals(user.getStatus()) && user.getPassword() != null && !user.getPassword().isEmpty();
            }
        });
    }

}
