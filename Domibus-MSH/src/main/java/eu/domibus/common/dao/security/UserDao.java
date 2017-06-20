package eu.domibus.common.dao.security;

import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Thomas Dussart
 * @since 3.3
 */
public interface UserDao {
    List<User> listUsers();
    void create(final User user);

    User loadUserByUsername(String userName);
}
