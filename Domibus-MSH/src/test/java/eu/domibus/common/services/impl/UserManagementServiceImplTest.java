package eu.domibus.common.services.impl;

import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.User;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusMessageCode;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.Properties;

import static eu.domibus.common.services.impl.UserManagementServiceImpl.MAXIMUM_LOGGIN_ATTEMPT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class UserManagementServiceImplTest {

    @Injectable
    private UserDao userDao;

    @Injectable
    private UserRoleDao userRoleDao;

    @Injectable
    private BCryptPasswordEncoder bcryptEncoder;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private Properties domibusProperties;

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
            userManagementService.logOnly(anyString, user);
            result = true;
        }};
        userManagementService.handleAuthenticationPolicy("");
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
            userManagementService.logOnly(anyString, user);
            result = false;
        }};
        userManagementService.handleAuthenticationPolicy("");
        new Verifications() {{
            userManagementService.applyAccountLockingPolicy(user);
            times = 1;
        }};
    }


    @Test
    public void applyAccountLockingPolicyBellowMaxAttempt(final @Mocked User user) {
        new Expectations() {{
            domibusProperties.getProperty(MAXIMUM_LOGGIN_ATTEMPT, "5");
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
            domibusProperties.getProperty(MAXIMUM_LOGGIN_ATTEMPT, "5");
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
            domibusProperties.getProperty(MAXIMUM_LOGGIN_ATTEMPT, "5");
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
    public void logOnlyUserNulle(@Mocked final DomibusLogger LOG, final @Mocked User user) {
        boolean test = userManagementService.logOnly("test", null);
        assertTrue(test);
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
        boolean test = userManagementService.logOnly("test", user);
        assertTrue(test);
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
        boolean test = userManagementService.logOnly("test", user);
        assertTrue(test);
        new Verifications() {{
            LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_SUSPENDED_USER, "test");
            times = 1;
        }};
    }

    @Test
    public void notLoging(final @Mocked User user) {
        new Expectations() {{
            user.isEnabled();
            result = true;
        }};
        boolean test = userManagementService.logOnly("test", user);
        assertFalse(test);
    }

}