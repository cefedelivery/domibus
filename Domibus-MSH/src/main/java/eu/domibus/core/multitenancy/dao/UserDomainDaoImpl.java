package eu.domibus.core.multitenancy.dao;


import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.User;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Repository
public class UserDomainDaoImpl extends BasicDao<UserDomainEntity> implements UserDomainDao {

    public UserDomainDaoImpl() {
        super(UserDomainEntity.class);
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

    @Override
    public String findPreferredDomainByUser(String userName) {
        TypedQuery<UserDomainEntity> namedQuery = em.createNamedQuery("UserDomainEntity.findByUserName", UserDomainEntity.class);
        namedQuery.setParameter("USER_NAME", userName);
        try {
            final UserDomainEntity userDomainEntity = namedQuery.getSingleResult();
            return userDomainEntity.getPreferredDomain();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<UserDomainEntity> listPreferredDomains() {
        TypedQuery<UserDomainEntity> namedQuery = em.createNamedQuery("UserDomainEntity.findPreferredDomains", UserDomainEntity.class);
        return namedQuery.getResultList();
    }
    
    @Override
    public void setDomainByUser(String userName, String domainCode) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity != null) {
            userDomainEntity.setDomain(domainCode);
            this.update(userDomainEntity);
        } else {
            userDomainEntity = new UserDomainEntity();
            userDomainEntity.setUserName(userName);
            userDomainEntity.setDomain(domainCode);
            this.create(userDomainEntity);
        }
    }
    
    @Override
    public void setPreferredDomainByUser(String userName, String domainCode) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity != null) {
            userDomainEntity.setPreferredDomain(domainCode);
            this.update(userDomainEntity);
        }  else {
            userDomainEntity = new UserDomainEntity();
            userDomainEntity.setUserName(userName);
            userDomainEntity.setPreferredDomain(domainCode);
            this.create(userDomainEntity); 
        }
    }

    @Override
    public List<String> listAllUserNames() {
        TypedQuery<UserDomainEntity> namedQuery = em.createNamedQuery("UserDomainEntity.findAll", UserDomainEntity.class);
        return namedQuery.getResultList().stream().map(el->el.getUserName()).collect(Collectors.toList());
    }

    private UserDomainEntity findUserDomainEntity(String userName) {
        TypedQuery<UserDomainEntity> namedQuery = em.createNamedQuery("UserDomainEntity.findByUserName", UserDomainEntity.class);
        namedQuery.setParameter("USER_NAME", userName);
        try {
            final UserDomainEntity userDomainEntity = namedQuery.getSingleResult();
            return userDomainEntity;
        } catch (NoResultException e) {
            return null;
        }
    }

}

