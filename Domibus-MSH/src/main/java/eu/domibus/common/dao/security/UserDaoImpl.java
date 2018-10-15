package eu.domibus.common.dao.security;


import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.User;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 * <p>
 * Dao to handle admin console users.
 */
@Repository
public class UserDaoImpl extends BasicDao<User> implements UserDao {

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    public List<User> listUsers() {
        TypedQuery<User> namedQuery = em.createNamedQuery("User.findAll", User.class);
        return namedQuery.getResultList();
    }

    @Override
    public List<User> getSuspendedUsers(final Date currentTimeMinusSuspensionInterval) {
        TypedQuery<User> namedQuery = em.createNamedQuery("User.findSuspendedUsers", User.class);
        namedQuery.setParameter("SUSPENSION_INTERVAL", currentTimeMinusSuspensionInterval);
        return namedQuery.getResultList();
    }

    @Override
    public User loadUserByUsername(String userName) {
        TypedQuery<User> namedQuery = em.createNamedQuery("User.findByUserName", User.class);
        namedQuery.setParameter("USER_NAME", userName);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public User loadActiveUserByUsername(String userName) {
        TypedQuery<User> namedQuery = em.createNamedQuery("User.findActiveByUserName", User.class);
        namedQuery.setParameter("USER_NAME", userName);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void update(final List<User> users) {
        for (final User u : users) {
            super.update(u);
        }
    }

    @Override
    public void delete(final Collection<User> users){
        for (final User u : users) {
            u.setDeleted(true);
            super.update(u);
        }
    }

    @Override
    public List<User> findWithPasswordChangedBetween(LocalDate start, LocalDate end) {
        TypedQuery<User> namedQuery = em.createNamedQuery("User.findWithPasswordChangedBetween", User.class);
        namedQuery.setParameter("START_DATE", start.atStartOfDay());
        namedQuery.setParameter("END_DATE", end.atStartOfDay());
        return namedQuery.getResultList();
    }

}
