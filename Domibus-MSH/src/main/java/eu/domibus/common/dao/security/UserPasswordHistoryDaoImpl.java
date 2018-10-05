package eu.domibus.common.dao.security;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserPasswordHistory;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Date;
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

    public Date findPasswordDate(final User user) {
        List<UserPasswordHistory> oldEntries = getPasswordHistoryEntries(user, 1);
        return oldEntries.stream().map(h -> h.getPasswordDate()).findFirst().orElse(null);

//        TypedQuery<UserPasswordHistory> namedQuery = em.createNamedQuery("UserPasswordHistory.findPasswords", UserPasswordHistory.class);
//        namedQuery.setParameter("USER", user);
//        return namedQuery.setMaxResults(1).getResultList().stream().map(h -> h.getPasswordDate()).findFirst().orElse(null);
    }

    public void savePassword(final User user, String passwordHash) {
        UserPasswordHistory entry = new UserPasswordHistory(user, passwordHash);
        this.update(entry);
    }

    public void removePasswords(final User user, int passwordsToKeep) {
//        TypedQuery<UserPasswordHistory> namedQuery = em.createNamedQuery("UserPasswordHistory.findPasswords", UserPasswordHistory.class);
//        namedQuery.setParameter("USER", user);
//        UserPasswordHistory[] oldEntries = namedQuery.getResultList().toArray(new UserPasswordHistory[] {});

        UserPasswordHistory[] oldEntries = getPasswordHistoryEntries(user, 0).toArray(new UserPasswordHistory[]{});
        if (oldEntries.length > passwordsToKeep) {
            Arrays.stream(oldEntries).skip(passwordsToKeep).forEach(entry -> this.delete(entry));
        }
    }

    @Override
    public List<UserPasswordHistory> getPasswordHistory(User user, int entriesNumber) {
        return getPasswordHistoryEntries(user, entriesNumber);
    }

    private List<UserPasswordHistory> getPasswordHistoryEntries(User user, int maxResults) {
        TypedQuery<UserPasswordHistory> namedQuery = em.createNamedQuery("UserPasswordHistory.findPasswords", UserPasswordHistory.class);
        namedQuery.setParameter("USER", user);
        List<UserPasswordHistory> oldEntries;
        if (maxResults > 0) {
            oldEntries = namedQuery.setMaxResults(maxResults).getResultList();
        } else {
            oldEntries = namedQuery.getResultList();
        }
        return oldEntries;
    }
}
