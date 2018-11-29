package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.User;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.services.UserPersistenceService;
import eu.domibus.common.validators.ConsoleUserPasswordManager;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@RunWith(JMockit.class)
public class SuperUserManagementServiceImplTest {

    @Injectable
    DomainService domainService;

    @Injectable
    EventService eventService;

    @Injectable
    MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Injectable
    UserPersistenceService userPersistenceService;

    @Injectable
    UserConverter userConverter;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    UserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    protected UserRoleDao userRoleDao;

    @Injectable
    protected UserDao userDao;

    @Injectable
    protected UserDomainService userDomainService;

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Mocked
    private UserManagementServiceImpl userManagementService;

    @Injectable
    ConsoleUserPasswordManager userPasswordValidator;


    @Tested
    private SuperUserManagementServiceImpl superUserManagementService;

    @Test
    public void findUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User() {{
            setUserName("user1");
        }});
        List<User> superUsers = new ArrayList<>();
        superUsers.add(new User() {{
            setUserName("super1");
        }});
        new Expectations() {{
            userManagementService.findUsers();
            result = users;
            userDomainService.getSuperUsers();
            result = superUsers;
        }};

        List<User> all = superUserManagementService.findUsers();

        assertEquals(all.size(), 2);
        assertEquals(all.get(0).getUserName(), "user1");
        assertEquals(all.get(1).getUserName(), "super1");
    }

    @Test
    public void updateUsers() {
        User user = new User() {{
            setUserName("user1");
            setAuthorities(Arrays.asList(AuthRole.ROLE_USER.toString()));
        }};

        User sUser = new User() {{
            setUserName("super1");
            setAuthorities(Arrays.asList(AuthRole.ROLE_AP_ADMIN.toString()));
        }};

        List<User> all = Arrays.asList(user, sUser);
        List<User> users = Arrays.asList(user);

        superUserManagementService.updateUsers(all);

        new Verifications() {{
            userManagementService.updateUsers(users);
            times = 1;
        }};
    }

    @Test
    public void changePassword(@Mocked UserManagementServiceImpl userManagementService) {
        String username = "u1", currentPassword = "pass1", newPassword = "newPass1";

        superUserManagementService.changePassword(username, currentPassword, newPassword);

        new Verifications() {{
            domainTaskExecutor.submit((Runnable)any);
            times = 1;
        }};
    }


}
