package eu.domibus.common.dao.security;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.UserBase;
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
public abstract class UserPasswordHistoryDaoImpl<U extends UserBase, E extends UserPasswordHistory> extends BasicDao<E>
        implements UserPasswordHistoryDao<U> {

    private final Class<E> typeOfE;
    private String passwordHistoryQueryName;

    protected abstract E createNew(final U user, String passwordHash, LocalDateTime passwordDate);

    public UserPasswordHistoryDaoImpl(Class<E> typeOfE, String passwordHistoryQueryName) {
        super(typeOfE);
        this.typeOfE = typeOfE;
        this.passwordHistoryQueryName = passwordHistoryQueryName;
    }

    @Override
    public void savePassword(final U user, String passwordHash, LocalDateTime passwordDate) {
        E entry = createNew(user, passwordHash, passwordDate);
        this.update(entry);
    }

    @Override
    public void removePasswords(final U user, int oldPasswordsToKeep) {
        List<UserPasswordHistory> oldEntries = getPasswordHistory(user, 0);
        if (oldEntries.size() > oldPasswordsToKeep) {
            oldEntries.stream().skip(oldPasswordsToKeep).forEach(entry -> this.delete((E) entry)); // NOSONAR
        }
    }

    @Override
    public List<UserPasswordHistory> getPasswordHistory(U user, int entriesCount) {
        TypedQuery<UserPasswordHistory> namedQuery = em.createNamedQuery(passwordHistoryQueryName, UserPasswordHistory.class);
        namedQuery.setParameter("USER", user);
        if (entriesCount > 0) {
            namedQuery = namedQuery.setMaxResults(entriesCount);
        }
        return namedQuery.getResultList();
    }
}

