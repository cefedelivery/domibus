package eu.domibus.common.services.impl;

import com.google.common.collect.Lists;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusMessageCode;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.List;

import static eu.domibus.common.services.impl.UserManagementServiceImpl.LOGIN_SUSPENSION_TIME;
import static eu.domibus.common.services.impl.UserManagementServiceImpl.MAXIMUM_LOGIN_ATTEMPT;
import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class UserManagementServiceImplTest {

    @Injectable
    private UserDao userDao;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private UserRoleDao userRoleDao;

    @Injectable
    private BCryptPasswordEncoder bcryptEncoder;

    @Injectable
    private DomainCoreConverter domainConverter;
    
    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private UserDomainService userDomainService;

    @Injectable
    private UserConverter userConverter;

    @Tested
    private UserManagementServiceImpl userService;

    @Test
    public void getLoggedUserNamed(@Mocked SecurityContextHolder securityContextHolder, @Mocked Authentication authentication) throws Exception {
        new Expectations() {{
            securityContextHolder.getContext().getAuthentication();
            result = authentication;
            authentication.getName();
            result = "thomas";
        }};
        String loggedUserNamed = userService.getLoggedUserNamed();
        assertEquals("thomas", loggedUserNamed);
    }

    @Test
    public void getLoggedUserNamedWithNoAuthentication(@Mocked SecurityContextHolder securityContextHolder) throws Exception {
        new Expectations() {{
            SecurityContextHolder.getContext().getAuthentication();
            result = null;
        }};
        String loggedUserNamed = userService.getLoggedUserNamed();
        assertNull(loggedUserNamed);
    }

    @Tested
    private UserManagementServiceImpl userManagementService;

    @Test
    public void findUsers() throws Exception {
        //TODO
    }

    @Test
    public void saveUsers() throws Exception {
        //TODO
    }

    @Test
    public void findUserRoles() throws Exception {
        //TODO
    }

    @Test
    public void updateUsers() throws Exception {
        //TODO
    }

    @Test
    public void handleAuthenticationPolicyNoAttemptUpgrade(final @Mocked User user) throws Exception {
        new Expectations(userManagementService) {{
            userDao.loadUserByUsername(anyString);
            result = user;
            userManagementService.canApplyAccountLockingPolicy(anyString, user);
            result = UserLoginErrorReason.UNKNOWN;
        }};
        userManagementService.handleWrongAuthentication("");
        new Verifications() {{
            userManagementService.applyAccountLockingPolicy(user);
            times = 0;
        }};
    }

    @Test
    public void handleAuthenticationPolicyWithAttemptUpgrade(final @Mocked User user) throws Exception {
        new Expectations(userManagementService) {{
            userDao.loadUserByUsername(anyString);
            result = user;
            userManagementService.canApplyAccountLockingPolicy(anyString, user);
            result = UserLoginErrorReason.BAD_CREDENTIALS;
        }};
        userManagementService.handleWrongAuthentication("");
        new Verifications() {{
            userManagementService.applyAccountLockingPolicy(user);
            times = 1;
        }};
    }


    @Test
    public void applyAccountLockingPolicyBellowMaxAttempt(final @Mocked User user) {
        new Expectations() {{
            domibusPropertyProvider.getProperty(MAXIMUM_LOGIN_ATTEMPT, "5");
            times = 1;
            result = 2;
            user.getAttemptCount();
            times = 2;
            result = 0;
        }};
        userManagementService.applyAccountLockingPolicy(user);
        new Verifications() {{
            user.setActive(withAny(true));
            times = 0;
            user.setSuspensionDate(withAny(new Date()));
            times = 0;
            userDao.update(user);
            times = 1;
        }};
    }

    @Test
    public void applyAccountLockingPolicyNotNumberProperty(final @Mocked User user) {
        new Expectations() {{
            domibusPropertyProvider.getProperty(MAXIMUM_LOGIN_ATTEMPT, "5");
            times = 1;
            result = "a";
            user.getAttemptCount();
            times = 2;
            result = 0;
        }};
        userManagementService.applyAccountLockingPolicy(user);
        new Verifications() {{
            user.setActive(withAny(true));
            times = 0;
            user.setSuspensionDate(withAny(new Date()));
            times = 0;
            userDao.update(user);
            times = 1;
        }};
    }

    @Test
    public void applyAccountLockingPolicyReachMaxAttempt(final @Mocked User user) {
        new Expectations() {{
            domibusPropertyProvider.getProperty(MAXIMUM_LOGIN_ATTEMPT, "5");
            times = 1;
            result = 2;
            user.getAttemptCount();
            result = 1;
            user.getAttemptCount();
            result = 2;
        }};
        userManagementService.applyAccountLockingPolicy(user);
        new Verifications() {{
            user.setActive(false);
            times = 1;
            user.setSuspensionDate(withAny(new Date()));
            times = 1;
            userDao.update(user);
            times = 1;
        }};
    }

    @Test
    public void logOnlyUserNull(@Mocked final DomibusLogger LOG, final @Mocked User user) {
        UserLoginErrorReason userLoginErrorReason = userManagementService.canApplyAccountLockingPolicy("test", null);
        assertEquals(UserLoginErrorReason.UNKNOWN, userLoginErrorReason);
        new Verifications() {{
            LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_UNKNOWN_USER, "test");
            times = 1;
        }};

    }

    @Test
    public void logOnlyInactive(@Mocked final DomibusLogger LOG, final @Mocked User user) {
        new Expectations() {{
            user.isEnabled();
            result = false;
            user.getSuspensionDate();
            result = null;
        }};
        UserLoginErrorReason userLoginErrorReason = userManagementService.canApplyAccountLockingPolicy("test", user);
        assertEquals(UserLoginErrorReason.INACTIVE, userLoginErrorReason);
        new Verifications() {{
            LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_INACTIVE_USER, "test");
            times = 1;
        }};
    }

    @Test
    public void logOnlySuspended(@Mocked final DomibusLogger LOG, final @Mocked User user) {
        new Expectations() {{
            user.isEnabled();
            result = false;
            user.getSuspensionDate();
            result = new Date();
        }};
        UserLoginErrorReason userLoginErrorReason = userManagementService.canApplyAccountLockingPolicy("test", user);
        assertEquals(UserLoginErrorReason.SUSPENDED, userLoginErrorReason);
        new Verifications() {{
            LOG.securityWarn(DomibusMessageCode.SEC_CONSOLE_LOGIN_SUSPENDED_USER, "test");
            times = 1;
        }};
    }

    @Test
    public void notLoging(final @Mocked User user) {
        new Expectations() {{
            user.isEnabled();
            result = true;
        }};
        UserLoginErrorReason test = userManagementService.canApplyAccountLockingPolicy("test", user);
        assertEquals(UserLoginErrorReason.BAD_CREDENTIALS,test);
    }

    @Test
    public void undoUserSuspension(@Mocked final System system) {

        final long currentTime = 1510064395598L;
        final int suspensionInterval = 3600;
        final Date currentTimeMinusSuspensionInterval = new Date(currentTime - suspensionInterval * 1000);

        final User user = new User();
        user.setActive(false);
        user.setAttemptCount(2);
        user.setSuspensionDate(new Date());

        final List<User> users = Lists.newArrayList(user);

        new Expectations() {{
            domibusPropertyProvider.getProperty(LOGIN_SUSPENSION_TIME, "3600");
            times = 1;
            result = suspensionInterval;
            System.currentTimeMillis();
            result = currentTime;
            userDao.getSuspendedUser(currentTimeMinusSuspensionInterval);
            times = 1;
            result = users;
        }};

        userManagementService.findAndReactivateSuspendedUsers();

        new Verifications() {{
            List<User> users;
            userDao.update(users = withCapture());
            times = 1;
            User modifiedUser = users.get(0);
            assertTrue(modifiedUser.getActive());
            assertNull(modifiedUser.getSuspensionDate());
            assertEquals(0, modifiedUser.getAttemptCount(), 0d);
        }};

    }

    @Test
    public void prepareUserForUpdate() {
        final User userEntity = new User();
        userEntity.setActive(false);
        userEntity.setSuspensionDate(new Date());
        userEntity.setAttemptCount(5);
        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        user.setActive(true);
        new Expectations() {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;
        }};
        User user1 = userManagementService.prepareUserForUpdate(user);
        assertNull(user1.getSuspensionDate());
        assertEquals(0, user1.getAttemptCount(), 0d);
    }

    @Test
    public void handleCorrectAuthenticationWithSomeFaileAttempts(){
        final String userName="user";
        final User userEntity = new User();
        userEntity.setActive(true);
        userEntity.setAttemptCount(1);
        new Expectations() {{
            userDao.loadActiveUserByUsername(userName);
            result = userEntity;
        }};
        userManagementService.handleCorrectAuthentication(userName);
        new Verifications(){{
           User user;
           userDao.update(user=withCapture());times=1;
           assertEquals(0,user.getAttemptCount(),0d);
        }};
    }

    @Test
    public void handleCorrectAuthenticationWithOutSomeFaileAttempts(){
        final String userName="user";
        final User userEntity = new User();
        userEntity.setActive(true);
        userEntity.setAttemptCount(0);
        new Expectations() {{
            userDao.loadActiveUserByUsername(userName);
            result = userEntity;
        }};
        userManagementService.handleCorrectAuthentication(userName);
        new Verifications(){{
            userDao.update(withAny(new User()));times=0;
        }};
    }

}