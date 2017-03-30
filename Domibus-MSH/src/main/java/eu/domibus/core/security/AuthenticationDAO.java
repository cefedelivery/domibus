package eu.domibus.core.security;

import eu.domibus.api.security.AuthRole;
import eu.domibus.common.dao.BasicDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class AuthenticationDAO extends BasicDao<AuthenticationEntry> {

    public AuthenticationDAO() {
        super(AuthenticationEntry.class);
    }


    public AuthenticationEntry findByUser(final String username) {
        final TypedQuery<AuthenticationEntry> query = this.em.createNamedQuery("AuthenticationEntry.findByUsername", AuthenticationEntry.class);
        query.setParameter("USERNAME", username);

        return query.getSingleResult();
    }

    public List<AuthRole> getRolesForUser(final String username) {
        final TypedQuery<String> query = this.em.createNamedQuery("AuthenticationEntry.getRolesForUsername", String.class);
        query.setParameter("USERNAME", username);

        List<AuthRole> authRoles = new ArrayList<>();
        String rolesStr = query.getSingleResult();
        String[] roles = StringUtils.split(rolesStr, ';');
        for(String role : roles) {
            authRoles.add(AuthRole.valueOf(StringUtils.strip(role)));
        }
        return authRoles;
    }

    public AuthenticationEntry findByCertificateId(final String certificateId) {
        final TypedQuery<AuthenticationEntry> query = this.em.createNamedQuery("AuthenticationEntry.findByCertificateId", AuthenticationEntry.class);
        query.setParameter("CERTIFICATE_ID", certificateId);

        return query.getSingleResult();
    }

    public List<AuthRole> getRolesForCertificateId(final String certificateId) {
        final TypedQuery<String> query = this.em.createNamedQuery("AuthenticationEntry.getRolesForCertificateId", String.class);
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
