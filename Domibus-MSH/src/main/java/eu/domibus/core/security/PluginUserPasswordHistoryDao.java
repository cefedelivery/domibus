package eu.domibus.core.security;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ion Perpegel
 *
 * @since 4.1
 */
public interface PluginUserPasswordHistoryDao {

    void savePassword(final AuthenticationEntity user, String passwordHash, LocalDateTime passwordDate);

    void removePasswords(final AuthenticationEntity user, int oldPasswordsToKeep);

    List<PluginUserPasswordHistory> getPasswordHistory(final AuthenticationEntity user, int entriesCount);

    //List<String> getPasswordHistory2(String userName, int entriesCount);
}
