package eu.domibus.api.user;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class UserTest {

    @Test
    public void user() {
        User user = new User("userName", "email", false, new ArrayList<String>(), UserState.UPDATED, null, false);
        assertFalse(user.isSuspended());
        user = new User("userName", "email", false, new ArrayList<String>(), UserState.UPDATED, new Date(), false);
        assertTrue(user.isSuspended());
    }

}