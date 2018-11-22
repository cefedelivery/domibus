package eu.domibus.core.security;

import eu.domibus.common.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Repository
public class PluginUserPasswordHistoryDaoImpl extends BasicDao<PluginUserPasswordHistory> implements PluginUserPasswordHistoryDao {

    public PluginUserPasswordHistoryDaoImpl() {
        super(PluginUserPasswordHistory.class);
    }

    public void savePassword(final AuthenticationEntity user, String passwordHash, LocalDateTime passwordDate) {
        PluginUserPasswordHistory entry = new PluginUserPasswordHistory(user, passwordHash, passwordDate);
        this.update(entry);
    }

    public void removePasswords(final AuthenticationEntity user, int oldPasswordsToKeep) {
        PluginUserPasswordHistory[] oldEntries = getPasswordHistory(user, 0).toArray(new PluginUserPasswordHistory[]{});
        if (oldEntries.length > oldPasswordsToKeep) {
            Arrays.stream(oldEntries).skip(oldPasswordsToKeep).forEach(entry -> this.delete(entry)); // NOSONAR
        }
    }

    @Override
    public List<PluginUserPasswordHistory> getPasswordHistory(AuthenticationEntity user, int entriesCount) {
        TypedQuery<PluginUserPasswordHistory> namedQuery = em.createNamedQuery("PluginUserPasswordHistory.findPasswords", PluginUserPasswordHistory.class);
        namedQuery.setParameter("USER", user);
        if (entriesCount > 0) {
            namedQuery = namedQuery.setMaxResults(entriesCount);
        }
        return namedQuery.getResultList();
    }
}
