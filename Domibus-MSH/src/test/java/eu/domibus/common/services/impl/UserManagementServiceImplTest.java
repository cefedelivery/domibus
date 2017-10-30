package eu.domibus.common.services.impl;

import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.core.converter.DomainCoreConverter;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

}