package eu.domibus.common.dao.security;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserPasswordHistory;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Date;

/**
 * @since 4.1
 */

@Repository
public class UserPasswordHistoryDaoImpl extends BasicDao<UserPasswordHistory> implements UserPasswordHistoryDao {

    public UserPasswordHistoryDaoImpl() {
        super(UserPasswordHistory.class);
    }

    public Date findPasswordDate(final User user) {
        TypedQuery<UserPasswordHistory> namedQuery = em.createNamedQuery("UserPasswordHistory.findPasswords", UserPasswordHistory.class);
        namedQuery.setParameter("USER", user);
        return namedQuery.setMaxResults(1).getResultList().stream().map(h -> h.getPasswordDate()).findFirst().orElse(null);
    }

    public void savePassword(final User user, String passwordHash) {
        UserPasswordHistory entry = new UserPasswordHistory(user, passwordHash);
        this.update(entry);
    }

    public void removePasswords(final User user, int passwordsToKeep) {
        TypedQuery<UserPasswordHistory> namedQuery = em.createNamedQuery("UserPasswordHistory.findPasswords", UserPasswordHistory.class);
        namedQuery.setParameter("USER", user);
        UserPasswordHistory[] oldEntries = namedQuery.getResultList().toArray(new UserPasswordHistory[] {});
        if (oldEntries.length > passwordsToKeep) {
            Arrays.stream(oldEntries).skip(passwordsToKeep).forEach(entry -> this.delete(entry));
        }
    }
}
