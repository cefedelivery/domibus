package eu.domibus.common.dao.security;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.model.security.UserRole;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Repository
public class UserRoleDaoImpl extends BasicDao<UserRole> implements UserRoleDao {
    public UserRoleDaoImpl() {
        super(UserRole.class);
    }

    @Override
    public List<UserRole> listRoles() {
        TypedQuery<UserRole> namedQuery = em.createNamedQuery("UserRole.findAll", UserRole.class);
        return namedQuery.getResultList();
    }

    @Override
    public UserRole findByName(final String roleName) {
        TypedQuery<UserRole> namedQuery = em.createNamedQuery("UserRole.findByName", UserRole.class);
        namedQuery.setParameter("ROLE_NAME", roleName.trim().toUpperCase());
        return namedQuery.getSingleResult();
    }
}
