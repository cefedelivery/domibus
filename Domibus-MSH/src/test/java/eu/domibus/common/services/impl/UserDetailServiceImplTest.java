package eu.domibus.common.services.impl;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.services.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RunWith(MockitoJUnitRunner.class)
public class UserDetailServiceImplTest {

    @Mock
    private UserDao userDao;
    @Mock
    private DomibusPropertyProvider domibusPropertyProvider;
    @Mock
    private UserService userService;
    @Spy
    private BCryptPasswordEncoder bcryptEncoder;
    @InjectMocks
    private UserDetailServiceImpl userDetailService;

    @Test
    public void loadUserByUsernameSuccessfully() throws Exception {
        User user = new User("admin", "whateverdifferentthandefaultpasswordhash");

        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(user);
        when(domibusPropertyProvider.getOptionalDomainProperty(eq(UserDetailServiceImpl.CHECK_DEFAULT_PASSWORD), anyString())).thenReturn("true");
        when(userService.validateDaysTillExpiration(eq("admin"))).thenReturn(90);

        UserDetail admin = (UserDetail) userDetailService.loadUserByUsername("admin");

        assertEquals("whateverdifferentthandefaultpasswordhash", admin.getPassword());
        assertEquals("admin", admin.getUsername());
        assertEquals(false, admin.isDefaultPasswordUsed());
    }

    @Test
    public void loadUserByUsernameSuccessfullyUsingDefaultPassword() throws Exception {
        User user = new User("user", "$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36");

        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(user);
        when(domibusPropertyProvider.getOptionalDomainProperty(eq(UserDetailServiceImpl.CHECK_DEFAULT_PASSWORD), anyString())).thenReturn("true");
        when(userService.validateDaysTillExpiration(eq("admin"))).thenReturn(90);

        UserDetail admin = (UserDetail) userDetailService.loadUserByUsername("admin");

        assertEquals("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36", admin.getPassword());
        assertEquals("user", admin.getUsername());
        assertEquals(true, admin.isDefaultPasswordUsed());
    }

    @Test
    public void loadUserByUsernameSuccessfullyUsingDefaultPasswordWarningDisabled() throws Exception {
        User user = new User("user", "$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36");

        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(user);
        when(domibusPropertyProvider.getOptionalDomainProperty(eq(UserDetailServiceImpl.CHECK_DEFAULT_PASSWORD), anyString())).thenReturn("false");
        when(userService.validateDaysTillExpiration(eq("admin"))).thenReturn(90);

        UserDetail admin = (UserDetail) userDetailService.loadUserByUsername("admin");

        assertEquals("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36", admin.getPassword());
        assertEquals("user", admin.getUsername());
        assertEquals(false, admin.isDefaultPasswordUsed());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testUserNotFound() throws Exception {
        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(null);
        userDetailService.loadUserByUsername("adminNotInThere");
    }

}