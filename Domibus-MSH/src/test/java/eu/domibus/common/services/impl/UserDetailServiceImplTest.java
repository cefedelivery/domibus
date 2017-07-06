package eu.domibus.common.services.impl;

import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.Assert.assertEquals;
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
    @Spy
    private BCryptPasswordEncoder bcryptEncoder;
    @InjectMocks
    private UserDetailServiceImpl userDetailService;
    @Test
    public void loadUserByUsernameSuccesfully() throws Exception {
        User user = new User();
        user.setUserName("admin");
        user.setPassword("whateverdifferentthandefaultpasswordhash");
        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(user);
        UserDetail admin = (UserDetail) userDetailService.loadUserByUsername("admin");

        assertEquals("whateverdifferentthandefaultpasswordhash",admin.getPassword());
        assertEquals("admin",admin.getUsername());
        assertEquals(false,admin.isDefaultPasswordUsed());
    }

    @Test
    public void loadUserByUsernameSuccesfullyUsingDefaultPassword() throws Exception {
        User user = new User();
        user.setUserName("user");
        user.setPassword("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36");
        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(user);
        UserDetail admin = (UserDetail) userDetailService.loadUserByUsername("admin");

        assertEquals("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36",admin.getPassword());
        assertEquals("user",admin.getUsername());
        assertEquals(true,admin.isDefaultPasswordUsed());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testUserNotFound() throws Exception {
        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(null);
        userDetailService.loadUserByUsername("adminNotInThere");
    }

}