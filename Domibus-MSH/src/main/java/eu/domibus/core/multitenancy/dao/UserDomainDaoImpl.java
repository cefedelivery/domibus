package eu.domibus.core.multitenancy.dao;


import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.User;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;


/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Repository
public class UserDomainDaoImpl extends BasicDao<User> implements UserDomainDao {

    public UserDomainDaoImpl() {
        super(User.class);
    }

    @Override
    public String findDomainByUser(String userName) {
        TypedQuery<UserDomainEntity> namedQuery = em.createNamedQuery("UserDomainEntity.findByUserName", UserDomainEntity.class);
        namedQuery.setParameter("USER_NAME", userName);
        try {
            final UserDomainEntity userDomainEntity = namedQuery.getSingleResult();
            return userDomainEntity.getDomain();
        } catch (NoResultException e) {
            return null;
        }
    }
}

