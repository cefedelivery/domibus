
package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Repository
public class PartyDao extends BasicDao<Party> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyDao.class);

    public PartyDao() {
        super(Party.class);
    }

    public Collection<Identifier> findPartyIdentifiersByEndpoint(final String endpoint) {
        final Query query = this.em.createNamedQuery("Party.findPartyIdentifiersByEndpoint");
        query.setParameter("ENDPOINT", endpoint);

        return query.getResultList();
    }

    public List<Party> listParties(String name,
                                   String endPoint,
                                   String partyId,
                                   String process,
                                   int pargeStart,
                                   int pageSize) {
        TypedQuery<Party> query = em.createQuery(buildPartyListCriteria(name, endPoint, partyId, process));
        query.setFirstResult(pargeStart);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    protected CriteriaQuery<Party> buildPartyListCriteria(final String name,
                                                          final String endPoint,
                                                          final String partyId,
                                                          final String process
    ) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Party> criteriaQuery = criteriaBuilder.createQuery(Party.class);
        Root<Party> root = criteriaQuery.from(Party.class);
        criteriaQuery.select(root);
        where(name, endPoint, partyId, process, criteriaBuilder, criteriaQuery, root);
        return criteriaQuery;
    }

    protected void where(
            final String name,
            final String endPoint,
            final String partyId,
            final String process,
            final CriteriaBuilder criteriaBuilder,
            final CriteriaQuery criteriaQuery,
            final Root<Party> root) {

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotEmpty(name)) {
            Path<String> nameField = root.get("name");
            predicates.add(criteriaBuilder.like(nameField, "%" + name + "%"));
        }
        if (StringUtils.isNotEmpty(endPoint)) {
            Path<String> endPointField = root.get("endpoint");
            predicates.add(criteriaBuilder.like(endPointField, "%" + endPoint + "%"));
        }
        if (StringUtils.isNotEmpty(partyId)) {
            Path<String> partyIdField = root.get("identifiers").get("partyId");
            predicates.add(criteriaBuilder.like(partyIdField, "%" + partyId + "%"));
        }
        if (StringUtils.isNotEmpty(process)) {
            /*Path<Object> auditTargetField = root.get("id").get("auditTargetName");
            predicates.add(auditTargetField.in(auditTargets));*/
        }
        if (!predicates.isEmpty()) {
            criteriaQuery.where(predicates.toArray(new Predicate[]{}));
        }
    }

    void setEntityManager(EntityManager entityManager) {
        em = entityManager;
    }



}
