package eu.domibus.common.dao.security;

import eu.domibus.common.model.security.User;

import java.util.List;

/**
 *
 * @author Thomas Dussart
 * @since 3.3
 */
public interface UserDao {
    List<User> listUsers();
    void create(final User user);
}
