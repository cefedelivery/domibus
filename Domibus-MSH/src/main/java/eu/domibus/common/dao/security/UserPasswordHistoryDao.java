package eu.domibus.common.dao.security;

import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserPasswordHistory;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @since 4.1
 */
public interface UserPasswordHistoryDao {

    Date findPasswordDate(final User user);

    void savePassword(final User user, String passwordHash);

    void removePasswords(final User user, int passwordsToKeep);

    List<UserPasswordHistory> getPasswordHistory(final User user, int entriesNumber);

}
