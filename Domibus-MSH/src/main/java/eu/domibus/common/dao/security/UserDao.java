package eu.domibus.common.dao.security;

import eu.domibus.common.model.security.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public interface UserDao {
    List<User> listUsers();

    void create(final User user);

    List<User> getSuspendedUsers(Date currentTimeMinusSuspensionInterval);

    User loadUserByUsername(String userName);

    User loadActiveUserByUsername(String userName);

    void update(final User entity);

    void update(final List<User> users);

    void delete(final Collection<User> delete);

    void flush();

    List<User> findWithPasswordChangedBetween(LocalDate start, LocalDate end);
}
