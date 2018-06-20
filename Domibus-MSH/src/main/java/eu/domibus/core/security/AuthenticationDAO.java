package eu.domibus.core.security;

import eu.domibus.api.security.AuthRole;
import eu.domibus.common.dao.BasicDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
        for (String role : roles) {
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
        for (String role : roles) {
            authRoles.add(AuthRole.valueOf(StringUtils.strip(role)));
        }
        return authRoles;
    }

    public long countEntries(Map<String, Object> filters) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AuthenticationEntity> mle = cq.from(AuthenticationEntity.class);
        cq.select(cb.count(mle));
        List<Predicate> predicates = getPredicates(filters, cb, mle);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<Long> query = em.createQuery(cq);
        return query.getSingleResult();

    }

    public List<AuthenticationEntity> findPaged(final int from, final int max, final String sortColumn, final boolean asc, final Map<String, Object> filters) {
        final CriteriaBuilder cb = this.em.getCriteriaBuilder();
        final CriteriaQuery<AuthenticationEntity> cq = cb.createQuery(AuthenticationEntity.class);
        final Root<AuthenticationEntity> ele = cq.from(AuthenticationEntity.class);
        cq.select(ele);
        List<Predicate> predicates = getPredicates(filters, cb, ele);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (sortColumn != null) {
            if (asc) {
                cq.orderBy(cb.asc(ele.get(sortColumn)));
            } else {
                cq.orderBy(cb.desc(ele.get(sortColumn)));
            }

        }
        final TypedQuery<AuthenticationEntity> query = this.em.createQuery(cq);
        query.setFirstResult(from);
        query.setMaxResults(max);
        return query.getResultList();
    }

    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder cb, Root<AuthenticationEntity> ele) {
        List<Predicate> predicates = new ArrayList<>();
        for (final Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() == null || StringUtils.isEmpty((String) filter.getValue()) || StringUtils.isEmpty(filter.getKey()))
                continue;

            if (filter.getKey().equals("authType")) {
                if (filter.getValue().equals("CERTIFICATE"))
                    predicates.add(cb.isNotNull(ele.<String>get("certificateId")));
                else
                    predicates.add(cb.isNull(ele.<String>get("certificateId")));
            } else {
                predicates.add(cb.like(ele.<String>get(filter.getKey()), (String) filter.getValue()));
            }
        }
        return predicates;
    }

}
