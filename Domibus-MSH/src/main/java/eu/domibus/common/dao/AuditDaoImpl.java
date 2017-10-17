package eu.domibus.common.dao;

import eu.domibus.common.model.audit.Audit;
import eu.domibus.common.model.common.ModificationType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Repository
public class AuditDaoImpl implements AuditDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuditDaoImpl.class);

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    @Override
    public List<Audit> listAudit(Set<String> auditTargets,
                                 Set<String> actions,
                                 Set<String> users,
                                 Date from,
                                 Date to,
                                 int start,
                                 int max) {


        logCriteria(auditTargets, actions, users, from, to, start, max);
        TypedQuery<Audit> query = entityManager.createQuery(
                builAuditListCriteria(
                        auditTargets,
                        actions,
                        users,
                        from,
                        to));
        query.setFirstResult(start);
        query.setMaxResults(max);
        return query.getResultList();
    }

    private CriteriaQuery<Audit> builAuditListCriteria(Set<String> auditTargets,
                                                       Set<String> actions,
                                                       Set<String> users,
                                                       Date from,
                                                       Date to) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Audit> criteriaQuery = criteriaBuilder.createQuery(Audit.class);
        Root<Audit> root = criteriaQuery.from(Audit.class);
        criteriaQuery.select(root);
        where(auditTargets, actions, users, from, to, criteriaBuilder, criteriaQuery, root);
        return criteriaQuery;
    }

    private void logCriteria(Set<String> auditTargets, Set<String> actions, Set<String> users, Date from, Date to, int start, int max) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching audit for ");
            LOG.debug("target :");
            auditTargets.forEach(LOG::debug);
            LOG.debug("actions :");
            actions.forEach(LOG::debug);
            LOG.debug("users :");
            users.forEach(LOG::debug);
            LOG.debug("from :" + from);
            LOG.debug("to :" + to);
            LOG.debug("start :" + start);
            LOG.debug("max :" + max);
        }
    }

    @Override
    public Long countAudit(Set<String> auditTargets, Set<String> actions, Set<String> users, Date from, Date to) {
        TypedQuery<Long> query = entityManager.createQuery(
                builCountListCriteria(
                        auditTargets,
                        actions,
                        users,
                        from,
                        to));
        return query.getSingleResult();
    }

    private CriteriaQuery<Long> builCountListCriteria(Set<String> auditTargets,
                                                      Set<String> actions,
                                                      Set<String> users,
                                                      Date from,
                                                      Date to) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Audit> root = criteriaQuery.from(Audit.class);
        criteriaQuery.select(criteriaBuilder.count(root));
        where(auditTargets, actions, users, from, to, criteriaBuilder, criteriaQuery, root);
        return criteriaQuery;
    }

    private void where(Set<String> auditTargets, Set<String> actions, Set<String> users, Date from, Date to, CriteriaBuilder criteriaBuilder, CriteriaQuery criteriaQuery, Root<Audit> root) {
        final Set<String> modificationTypes = new HashSet<>();
        actions.forEach(action -> {
            Set<String> collect = Arrays.stream(ModificationType.values()).
                    filter(modificationType -> modificationType.getLabel().equals(action)).
                    map(Enum::name).
                    collect(Collectors.toSet());
            modificationTypes.addAll(collect);
        });
        if (CollectionUtils.isNotEmpty(modificationTypes)) {
            Path<Object> actionField = root.get("actions");
            Predicate in = actionField.in(modificationTypes);
            criteriaQuery.where(in);
        }
        if (CollectionUtils.isNotEmpty(auditTargets)) {
            Path<Object> auditTargetField = root.get("auditTargetName");
            Predicate in = auditTargetField.in(auditTargets);
            criteriaQuery.where(in);
        }
        if (CollectionUtils.isNotEmpty(users)) {
            Path<Object> userField = root.get("users");
            Predicate in = userField.in(users);
            criteriaQuery.where(in);
        }
        if (from != null) {
            Path<Date> changedDate = root.get("changed");
            criteriaBuilder.greaterThanOrEqualTo(changedDate, from);
        }
        if (to != null) {
            Path<Date> changedDate = root.get("changed");
            criteriaBuilder.lessThanOrEqualTo(changedDate, to);
        }
    }
}
