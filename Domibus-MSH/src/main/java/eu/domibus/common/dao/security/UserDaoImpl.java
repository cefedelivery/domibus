package eu.domibus.common.dao.security;


import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.User;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 *
 * Dao to handle admin console users.
 *
 */
@Repository
public class UserDaoImpl extends BasicDao<User> implements UserDao{

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    public List<User> listUsers() {
        TypedQuery<User> namedQuery = em.createNamedQuery("User.findAll", User.class);
        return namedQuery.getResultList();
    }

    @Override
    public User loadUserByUsername(String userName){
        TypedQuery<User> namedQuery = em.createNamedQuery("User.findByUserName", User.class);
        namedQuery.setParameter("USER_NAME",userName);
        return namedQuery.getSingleResult();
    }

    @Override
    public User loadActiveUserByUsername(String userName){
        TypedQuery<User> namedQuery = em.createNamedQuery("User.findActiveByUserName", User.class);
        namedQuery.setParameter("USER_NAME",userName);
        return namedQuery.getSingleResult();
    }
}
