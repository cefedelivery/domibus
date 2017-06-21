package eu.domibus.core.security;

import eu.domibus.api.security.AuthRole;
import eu.domibus.common.dao.BasicDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Repository("securityAuthenticationDAO")
@Transactional
public class AuthenticationDAO extends BasicDao<AuthenticationEntity> {

    public AuthenticationDAO() {
        super(AuthenticationEntity.class);
    }


    public AuthenticationEntity findByUser(final String username) {
        final TypedQuery<AuthenticationEntity> query = this.em.createNamedQuery("AuthenticationEntity.findByUsername", AuthenticationEntity.class);
        query.setParameter("USERNAME", username);

        return query.getSingleResult();
    }

    public List<AuthRole> getRolesForUser(final String username) {
        final TypedQuery<String> query = this.em.createNamedQuery("AuthenticationEntity.getRolesForUsername", String.class);
        query.setParameter("USERNAME", username);

        List<AuthRole> authRoles = new ArrayList<>();
        String rolesStr = query.getSingleResult();
        String[] roles = StringUtils.split(rolesStr, ';');
        for(String role : roles) {
            authRoles.add(AuthRole.valueOf(StringUtils.strip(role)));
        }
        return authRoles;
    }

    public AuthenticationEntity findByCertificateId(final String certificateId) {
        final TypedQuery<AuthenticationEntity> query = this.em.createNamedQuery("AuthenticationEntity.findByCertificateId", AuthenticationEntity.class);
        query.setParameter("CERTIFICATE_ID", certificateId);

        return query.getSingleResult();
    }

    public List<AuthRole> getRolesForCertificateId(final String certificateId) {
        final TypedQuery<String> query = this.em.createNamedQuery("AuthenticationEntity.getRolesForCertificateId", String.class);
        query.setParameter("CERTIFICATE_ID", certificateId);

        List<AuthRole> authRoles = new ArrayList<>();
        String rolesStr = query.getSingleResult();
        String[] roles = StringUtils.split(rolesStr, ';');
        for(String role : roles) {
            authRoles.add(AuthRole.valueOf(StringUtils.strip(role)));
        }
        return authRoles;
    }
}
