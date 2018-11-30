package eu.domibus.common.dao.security;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.ConsoleUserPasswordHistory;
import eu.domibus.common.model.security.User;
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
public class UserPasswordHistoryDaoImpl extends BasicDao<ConsoleUserPasswordHistory> implements UserPasswordHistoryDao {

    public UserPasswordHistoryDaoImpl() {
        super(ConsoleUserPasswordHistory.class);
    }

    public void savePassword(final User user, String passwordHash, LocalDateTime passwordDate) {
        ConsoleUserPasswordHistory entry = new ConsoleUserPasswordHistory(user, passwordHash, passwordDate);
        this.update(entry);
    }

    public void removePasswords(final User user, int oldPasswordsToKeep) {
        List<UserPasswordHistory> oldEntries = getPasswordHistory(user, 0);
        if (oldEntries.size() > oldPasswordsToKeep) {
            oldEntries.stream().skip(oldPasswordsToKeep).forEach(entry -> this.delete((ConsoleUserPasswordHistory) entry)); // NOSONAR
        }
    }

    @Override
    public List<UserPasswordHistory> getPasswordHistory(User user, int entriesCount) {
        TypedQuery<UserPasswordHistory> namedQuery = em.createNamedQuery("ConsoleUserPasswordHistory.findPasswords", UserPasswordHistory.class);
        namedQuery.setParameter("USER", user);
        if (entriesCount > 0) {
            namedQuery = namedQuery.setMaxResults(entriesCount);
        }
        return namedQuery.getResultList();
    }
}
