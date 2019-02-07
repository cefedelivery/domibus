package eu.domibus.common.services.impl;

import com.google.common.collect.Collections2;
import eu.domibus.api.multitenancy.UserDomain;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.common.dao.security.ConsoleUserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserPersistenceService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.security.ConsoleUserSecurityPolicyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class UserPersistenceServiceImpl implements UserPersistenceService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserPersistenceServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    private BCryptPasswordEncoder bCryptEncoder;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    private ConsoleUserSecurityPolicyManager securityPolicyManager;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUsers(List<eu.domibus.api.user.User> users) {
        // insertion
        Collection<eu.domibus.api.user.User> newUsers = filterNewUsers(users);
        LOG.debug("New users:" + newUsers.size());
        insertNewUsers(newUsers);

        // update
        Collection<eu.domibus.api.user.User> noPasswordChangedModifiedUsers = filterModifiedUserWithoutPasswordChange(users);
        LOG.debug("Modified users without password change:" + noPasswordChangedModifiedUsers.size());
        updateUsers(noPasswordChangedModifiedUsers, false);

        Collection<eu.domibus.api.user.User> passwordChangedModifiedUsers = filterModifiedUserWithPasswordChange(users);
        LOG.debug("Modified users with password change:" + passwordChangedModifiedUsers.size());
        updateUsers(passwordChangedModifiedUsers, true);

        // deletion
        List<eu.domibus.api.user.User> deletedUsers = filterDeletedUsers(users);
        LOG.debug("Users to delete:" + deletedUsers.size());
        deleteUsers(deletedUsers);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void changePassword(String userName, String currentPassword, String newPassword) {
        User userEntity = userDao.loadUserByUsername(userName);
        changePassword(userEntity, currentPassword, newPassword);
        userDao.update(userEntity);
    }

    protected void updateUsers(Collection<eu.domibus.api.user.User> users, boolean withPasswordChange) {
        for (eu.domibus.api.user.User user : users) {
            User existing = userDao.loadUserByUsername(user.getUserName());

            //suspension logic
            securityPolicyManager.applyLockingPolicyOnUpdate(user);

            if (withPasswordChange) {
                changePassword(existing, user.getPassword());
            }

            existing.setEmail(user.getEmail());
            existing.clearRoles();
            addRoleToUser(user.getAuthorities(), existing);

            userDao.update(existing);

            if (user.getAuthorities() != null && user.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name())) {
                userDomainService.setPreferredDomainForUser(user.getUserName(), user.getDomain());
            }
        }
    }

    protected void changePassword(User user, String currentPassword, String newPassword) {
        //check if old password matches the persisted one
        if (!bCryptEncoder.matches(currentPassword, user.getPassword())) {
            throw new UserManagementException("The current password does not match the provided one.");
        }

        changePassword(user, newPassword);
    }

    protected void changePassword(User user, String newPassword) {
        securityPolicyManager.changePassword(user, newPassword);
    }

    protected void insertNewUsers(Collection<eu.domibus.api.user.User> newUsers) {
        // validate user not already in general schema
        //get all users from user-domains table in general schema
        List<UserDomain> allUsers = userDomainService.getAllUserDomainMappings();
        for (eu.domibus.api.user.User user : newUsers) {
            List<UserDomain> existing = allUsers.stream().filter(userDomain -> userDomain.getUserName().equalsIgnoreCase(user.getUserName()))
                    .collect(Collectors.toList());

            if (!existing.isEmpty()) {
                UserDomain existingUser = existing.get(0);
                String errorMessage = "Cannot add user " + existingUser.getUserName() + " because this name already exists in the "
                        + existingUser.getDomain() + " domain.";
                throw new UserManagementException(errorMessage);
            }
        }

        for (eu.domibus.api.user.User user : newUsers) {
            securityPolicyManager.validateComplexity(user.getUserName(), user.getPassword());

            User userEntity = domainConverter.convert(user, User.class);

            userEntity.setPassword(bCryptEncoder.encode(userEntity.getPassword()));
            addRoleToUser(user.getAuthorities(), userEntity);
            userDao.create(userEntity);

            if (user.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name())) {
                userDomainService.setPreferredDomainForUser(user.getUserName(), user.getDomain());
            } else {
                userDomainService.setDomainForUser(user.getUserName(), user.getDomain());
            }
        }
    }

    protected void deleteUsers(List<eu.domibus.api.user.User> usersToDelete) {
        List<User> users = usersToDelete.stream()
                .map(user -> userDao.loadUserByUsername(user.getUserName()))
                .filter(user -> user != null)
                .collect(Collectors.toList());
        userDao.delete(users);
    }

    protected void addRoleToUser(List<String> authorities, User userEntity) {
        if (authorities == null || userEntity == null) {
            return;
        }
        for (String authority : authorities) {
            UserRole userRole = userRoleDao.findByName(authority);
            userEntity.addRole(userRole);
        }
    }


    private Collection<eu.domibus.api.user.User> filterNewUsers(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, user -> UserState.NEW.name().equals(user.getStatus()));
    }

    private Collection<eu.domibus.api.user.User> filterModifiedUserWithoutPasswordChange(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, user -> UserState.UPDATED.name().equals(user.getStatus()) && user.getPassword() == null);
    }

    private Collection<eu.domibus.api.user.User> filterModifiedUserWithPasswordChange(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, user -> UserState.UPDATED.name().equals(user.getStatus()) && user.getPassword() != null && !user.getPassword().isEmpty());
    }

    private List<eu.domibus.api.user.User> filterDeletedUsers(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, user -> UserState.REMOVED.name().equals(user.getStatus()))
                .stream().collect(Collectors.toList());
    }

}
