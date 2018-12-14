package eu.domibus.common.dao.security;

import eu.domibus.common.model.security.UserEntityBase;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public interface UserDaoBase {
    UserEntityBase findByUserName(String userName);

    List<UserEntityBase> findWithPasswordChangedBetween(LocalDate start, LocalDate end, boolean withDefaultPassword);

    void update(UserEntityBase user, boolean flush);

    List<UserEntityBase> getSuspendedUsers(Date currentTimeMinusSuspensionInterval);

    void update(List<UserEntityBase> users);

}
