package eu.domibus.core.security;

import eu.domibus.common.model.security.UserPasswordHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Ion Perpegel
 *
 * @since 4.1
 */
public interface PluginUserPasswordHistoryDao {

    void savePassword(final AuthenticationEntity user, String passwordHash, LocalDateTime passwordDate);

    void removePasswords(final AuthenticationEntity user, int oldPasswordsToKeep);

    List<UserPasswordHistory> getPasswordHistory(final AuthenticationEntity user, int entriesCount);

}
