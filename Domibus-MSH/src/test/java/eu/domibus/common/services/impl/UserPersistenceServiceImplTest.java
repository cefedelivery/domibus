package eu.domibus.common.services.impl;

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
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.security.ConsoleUserSecurityPolicyManager;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

/**
 * @author Thomas Dussart, Ion Perpegel
 * @since 4.0
 */
@RunWith(JMockit.class)
public class UserPersistenceServiceImplTest {

    @Injectable
    private UserDao userDao;

    @Injectable
    private UserRoleDao userRoleDao;

    @Injectable
    private ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    private BCryptPasswordEncoder bCryptEncoder;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private UserDomainService userDomainService;

    @Injectable
    private ConsoleUserSecurityPolicyManager policyManager;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Injectable
    private EventService eventService;

    @Autowired
    private DomainCoreConverter domainConverter2;


    @Tested
    private UserPersistenceServiceImpl userPersistenceService;

    @Test
    public void updateUsersTest() {
        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName("addedUserName");
            setActive(true);
            setStatus(UserState.NEW.name());
        }};
        List<eu.domibus.api.user.User> addedUsers = Arrays.asList(addedUser);
        User addedUserUntity = new User() {{
            setPassword("password1");
        }};

        eu.domibus.api.user.User modifiedUser = new eu.domibus.api.user.User() {{
            setUserName("modifiedUserName");
            setActive(true);
            setStatus(UserState.UPDATED.name());
        }};
        List<eu.domibus.api.user.User> modifiedUsers = Arrays.asList(modifiedUser);

        eu.domibus.api.user.User deletedUser = new eu.domibus.api.user.User() {{
            setUserName("deletedUserName");
            setActive(true);
            setStatus(UserState.REMOVED.name());
        }};
        List<eu.domibus.api.user.User> deletedUsers = Arrays.asList(deletedUser);

        List<eu.domibus.api.user.User> users = Arrays.asList(addedUser, modifiedUser, deletedUser);

        new Expectations() {{
            domainConverter.convert(addedUser, User.class);
            returns(addedUserUntity);
        }};

        userPersistenceService.updateUsers(users);

        new Verifications() {{
            userPersistenceService.updateUsers(modifiedUsers, false);
            times = 1;
            userPersistenceService.deleteUsers(deletedUsers);
            times = 1;
        }};
    }

    @Test
    public void insertNewUsersTest() {
        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName("addedUserName");
            setActive(true);
            setStatus(UserState.NEW.name());
        }};
        User addedUserUntity = new User() {{
            setPassword("password1");
        }};
        List<eu.domibus.api.user.User> addedUsers = Arrays.asList(addedUser);

        new Expectations() {{
            userDomainService.getAllUserDomainMappings();
            result = new ArrayList<>();
            domainConverter.convert(addedUser, User.class);
            result = addedUserUntity;
        }};

        userPersistenceService.insertNewUsers(addedUsers);

        new Verifications() {{
            userDao.create(addedUserUntity);
            times = 1;
            userDomainService.setDomainForUser(addedUser.getUserName(), addedUser.getDomain());
            times = 1;
            userDomainService.setPreferredDomainForUser(addedUser.getUserName(), addedUser.getDomain());
            times = 0;
        }};
    }


    @Test(expected = UserManagementException.class)
    public void insertNewUsersShouldFailIfUsernameAlreadyExists() {
        String testUsername = "testUsername";
        String testDomain = "testDomain";

        UserDomain existingUser = new UserDomain();
        existingUser.setUserName(testUsername);
        existingUser.setDomain(testDomain);
        List<UserDomain> existingUsers = Arrays.asList(existingUser);

        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName(testUsername);
            setActive(true);
            setStatus(UserState.NEW.name());
        }};
        List<eu.domibus.api.user.User> addedUsers = Arrays.asList(addedUser);

        new Expectations() {{
            userDomainService.getAllUserDomainMappings();
            result = existingUsers;
        }};

        userPersistenceService.insertNewUsers(addedUsers);
    }

    @Test
    public void internalUpdateUsersTest() {
        final User userEntity = new User() {{
            setActive(false);
            setSuspensionDate(new Date());
            setAttemptCount(5);
        }};
        eu.domibus.api.user.User user = new eu.domibus.api.user.User() {{
            setActive(true);
            setAuthorities(Arrays.asList(AuthRole.ROLE_AP_ADMIN.name()));
        }};
        Collection<eu.domibus.api.user.User> users = Arrays.asList(user);

        new Expectations() {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;
        }};

        userPersistenceService.updateUsers(users, true);

        new Verifications() {{
            policyManager.applyLockingPolicyOnUpdate(user);
            times = 1;
            policyManager.changePassword(userEntity, user.getPassword());
            times = 1;
            userEntity.setEmail(user.getEmail());
            times = 1;
            userPersistenceService.addRoleToUser(user.getAuthorities(), userEntity);
            times = 1;
            userDao.update(userEntity);
            times = 1;
            userDomainService.setPreferredDomainForUser(user.getUserName(), user.getDomain());
            times = 1;
        }};

    }

    @Test(expected = UserManagementException.class)
    public void changePasswordPasswordsNotMatchTest() {
        String userName = "user1";
        String currentPassword = "currentPassword";
        String newPassword = "newPassword";

        final User userEntity = new User() {{
            setActive(false);
            setSuspensionDate(new Date());
            setAttemptCount(5);
        }};

        new Expectations() {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;
        }};

        userPersistenceService.changePassword(userName, currentPassword, newPassword);

        new Verifications() {{
            userDao.update(userEntity);
            times = 1;
        }};

    }

    @Test
    public void changePasswordTest() {
        String userName = "user1";
        String currentPassword = "currentPassword";
        String newPassword = "newPassword";

        final User userEntity = new User() {{
            setActive(false);
            setSuspensionDate(new Date());
            setAttemptCount(5);
        }};

        new Expectations() {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;
            bCryptEncoder.matches(currentPassword, userEntity.getPassword());
            result = true;
        }};

        userPersistenceService.changePassword(userName, currentPassword, newPassword);

        new VerificationsInOrder() {{
            policyManager.changePassword(userEntity, newPassword);
            times = 1;
            userDao.update(userEntity);
            times = 1;
        }};

    }

}
