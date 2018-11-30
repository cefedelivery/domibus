package eu.domibus.core.security;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.UserPasswordHistory;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
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
        List<UserPasswordHistory> oldEntries = getPasswordHistory(user, 0);
        if (oldEntries.size() > oldPasswordsToKeep) {
            oldEntries.stream().skip(oldPasswordsToKeep).forEach(entry -> this.delete((PluginUserPasswordHistory) entry)); // NOSONAR
        }
    }

    @Override
    public List<UserPasswordHistory> getPasswordHistory(AuthenticationEntity user, int entriesCount) {
        TypedQuery<UserPasswordHistory> namedQuery = em.createNamedQuery("PluginUserPasswordHistory.findPasswords", UserPasswordHistory.class);
        namedQuery.setParameter("USER", user);
        if (entriesCount > 0) {
            namedQuery = namedQuery.setMaxResults(entriesCount);
        }
        return namedQuery.getResultList();
    }
}
