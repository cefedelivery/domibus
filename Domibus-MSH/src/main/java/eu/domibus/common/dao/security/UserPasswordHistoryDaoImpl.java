package eu.domibus.common.dao.security;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserPasswordHistory;
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
public class UserPasswordHistoryDaoImpl extends BasicDao<UserPasswordHistory> implements UserPasswordHistoryDao {

    public UserPasswordHistoryDaoImpl() {
        super(UserPasswordHistory.class);
    }

    public void savePassword(final User user, String passwordHash, LocalDateTime passwordDate) {
        UserPasswordHistory entry = new UserPasswordHistory(user, passwordHash, passwordDate);
        this.update(entry);
    }

    public void removePasswords(final User user, int oldPasswordsToKeep) {
        UserPasswordHistory[] oldEntries = getPasswordHistory(user, 0).toArray(new UserPasswordHistory[]{});
        if (oldEntries.length > oldPasswordsToKeep) {
            Arrays.stream(oldEntries).skip(oldPasswordsToKeep).forEach(entry -> this.delete(entry));
        }
    }

    @Override
    public List<UserPasswordHistory> getPasswordHistory(User user, int entriesCount) {
        TypedQuery<UserPasswordHistory> namedQuery = em.createNamedQuery("UserPasswordHistory.findPasswords", UserPasswordHistory.class);
        namedQuery.setParameter("USER", user);
        if (entriesCount > 0) {
            namedQuery = namedQuery.setMaxResults(entriesCount);
        }
        return namedQuery.getResultList();
    }
}
