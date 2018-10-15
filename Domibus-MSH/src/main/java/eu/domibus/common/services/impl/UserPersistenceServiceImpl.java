package eu.domibus.common.services.impl;

import com.google.common.collect.Collections2;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserPersistenceService;
import eu.domibus.common.validators.PasswordValidator;
import eu.domibus.core.alerts.model.service.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private UserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private PasswordValidator passwordValidator;

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

    private void updateUsers(Collection<eu.domibus.api.user.User> users, boolean withPasswordChange) {
        for (eu.domibus.api.user.User user : users) {
            User userEntity = prepareUserForUpdate(user);

            if (withPasswordChange) {
                savePasswordHistory(userEntity); // save old password in history
                passwordValidator.validateComplexity(user.getUserName(), user.getPassword());
                passwordValidator.validateHistory(user.getUserName(), user.getPassword());
                userEntity.setPassword(bcryptEncoder.encode(user.getPassword()));
            }
            addRoleToUser(user.getAuthorities(), userEntity);
            userDao.update(userEntity);

            if (user.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name())) {
                userDomainService.setPreferredDomainForUser(user.getUserName(), user.getDomain());
            }
        }
    }

    private void savePasswordHistory(User userEntity) {
        int passwordsToKeep = Integer.valueOf(domibusPropertyProvider.getOptionalDomainProperty(PasswordValidator.PASSWORD_HISTORY_POLICY, "0"));
        if (passwordsToKeep == 0) {
            return;
        }
        this.userPasswordHistoryDao.savePassword(userEntity, userEntity.getPassword(), userEntity.getPasswordChangeDate());
        this.userPasswordHistoryDao.removePasswords(userEntity, passwordsToKeep - 1);
    }

    private void insertNewUsers(Collection<eu.domibus.api.user.User> newUsers) {
        // validate user not already in general schema
        //get all users from user-domains table in general schema
        List<String> allUserNames = userDomainService.getAllUserNames();
        for (eu.domibus.api.user.User user : newUsers) {
            if (allUserNames.stream().anyMatch(name -> name.equalsIgnoreCase(user.getUserName()))) {
                String errorMessage = "Cannot add user " + user.getUserName() + " because this name already exists in the "
                        + user.getDomain() + " domain.";
                if (user.isDeleted()) {
                    errorMessage += "(it is deleted)";
                }
                throw new UserManagementException(errorMessage);
            }
        }

        for (eu.domibus.api.user.User user : newUsers) {
            passwordValidator.validateComplexity(user.getUserName(), user.getPassword());

            User userEntity = domainConverter.convert(user, User.class);

            userEntity.setPassword(bcryptEncoder.encode(userEntity.getPassword()));
            addRoleToUser(user.getAuthorities(), userEntity);
            userDao.create(userEntity);

            if (user.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name())) {
                userDomainService.setPreferredDomainForUser(user.getUserName(), user.getDomain());
            } else {
                userDomainService.setDomainForUser(user.getUserName(), user.getDomain());
            }
        }
    }

    private void deleteUsers(List<eu.domibus.api.user.User> usersToDelete) {
        List<User> users = usersToDelete.stream()
                .map(user -> userDao.loadUserByUsername(user.getUserName()))
                .filter(user -> user != null)
                .collect(Collectors.toList());
        userDao.delete(users);
    }

    private void addRoleToUser(List<String> authorities, User userEntity) {
        for (String authority : authorities) {
            UserRole userRole = userRoleDao.findByName(authority);
            userEntity.addRole(userRole);
        }
    }

    protected User prepareUserForUpdate(eu.domibus.api.user.User user) {
        User userEntity = userDao.loadUserByUsername(user.getUserName());
        if (!userEntity.getActive() && user.isActive()) {
            userEntity.setSuspensionDate(null);
            userEntity.setAttemptCount(0);
        }
        if (!user.isActive() && userEntity.getActive()) {
            LOG.debug("User:[{}] has been disabled by administrator", user.getUserName());
            //TODO trigger events for super user in 4.1 EDELIVERY-3768
            if (!user.isSuperAdmin()) {
                final AccountDisabledModuleConfiguration accountDisabledConfiguration = multiDomainAlertConfigurationService.getAccountDisabledConfiguration();
                if (accountDisabledConfiguration.isActive()) {
                    LOG.debug("Sending account disabled event for user:[{}]", user.getUserName());
                    eventService.enqueueAccountDisabledEvent(user.getUserName(), new Date(), true);
                }
            }
        }
        userEntity.setActive(user.isActive());
        userEntity.setEmail(user.getEmail());
        userEntity.clearRoles();
        return userEntity;
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
