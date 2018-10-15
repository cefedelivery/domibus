package eu.domibus.common.dao.security;

import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserPasswordHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @since 4.1
 */
public interface UserPasswordHistoryDao {

    void savePassword(final User user, String passwordHash, LocalDateTime passwordDate);

    void removePasswords(final User user, int oldPasswordsToKeep);

    List<UserPasswordHistory> getPasswordHistory(final User user, int entriesCount);

}
