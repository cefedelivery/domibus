package eu.domibus.common.dao.security;

import eu.domibus.AbstractIT;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class UserDaoImplTest extends AbstractIT{
    @Autowired
    private UserDao userDao;

    @Test
    @Transactional
    @Rollback
    public void listUsers() throws Exception {
        User user=new User("userOne", "test");
        UserRole userRole=new UserRole("ROLE_USER");
        user.addRole(userRole);
        user.setEmail("test@gmail.com");
        user.setActive(true);
        userDao.create(user);
        List<User> users = userDao.listUsers();
        assertEquals(1,users.size());
        user = users.get(0);
        assertEquals("test@gmail.com",user.getEmail());
        assertEquals("test",user.getPassword());
        assertEquals(true,user.isEnabled());
    }

    @Test
    @Transactional
    @Rollback
    public void loadActiveUserByUsername() {
        User user = new User("userTwo", "test");
        UserRole userRole = new UserRole("ROLE_USER");
        user.addRole(userRole);
        user.setEmail("test@gmail.com");
        user.setActive(true);
        userDao.create(user);
        final User userOne = userDao.loadActiveUserByUsername("userTwo");
        assertNotNull(userOne);
    }

}