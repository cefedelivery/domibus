package eu.domibus.common.dao.security;

import eu.domibus.common.model.security.UserBase;
import eu.domibus.common.model.security.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public interface UserDao extends UserDaoBase {
    List<User> listUsers();

    void create(final User user);

    List<User> getSuspendedUsers(Date currentTimeMinusSuspensionInterval);

    User loadUserByUsername(String userName);

    User loadActiveUserByUsername(String userName);

    void update(final User entity);

    void update(final List<User> users);

    void delete(final Collection<User> delete);

    void flush();
}
