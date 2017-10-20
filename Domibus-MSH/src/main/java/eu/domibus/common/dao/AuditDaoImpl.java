package eu.domibus.common.dao;

import eu.domibus.common.model.audit.Audit;
import eu.domibus.common.model.audit.JmsMessageAudit;
import eu.domibus.common.model.audit.MessageAudit;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
    public List<Audit> listAudit(final Set<String> auditTargets,
                                 final Set<String> actions,
                                 final Set<String> users,
                                 final Date from,
                                 final Date to,
                                 final int start,
                                 final int max) {

        logCriteria(auditTargets, actions, users, from, to, start, max);
        TypedQuery<Audit> query = entityManager.createQuery(
                buildAuditListCriteria(
                        auditTargets,
                        actions,
                        users,
                        from,
                        to));
        query.setFirstResult(start);
        query.setMaxResults(max);
        return query.getResultList();
    }

    private CriteriaQuery<Audit> buildAuditListCriteria(final Set<String> auditTargets,
                                                        final Set<String> actions,
                                                        final Set<String> users,
                                                        final Date from,
                                                        final Date to) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Audit> criteriaQuery = criteriaBuilder.createQuery(Audit.class);
        Root<Audit> root = criteriaQuery.from(Audit.class);
        criteriaQuery.select(root);
        where(auditTargets, actions, users, from, to, criteriaBuilder, criteriaQuery, root);
        return criteriaQuery;
    }

    private void logCriteria(final Set<String> auditTargets,
                             final Set<String> actions,
                             final Set<String> users,
                             final Date from,
                             final Date to,
                             final int start,
                             final int max) {
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
    public Long countAudit(final Set<String> auditTargets,
                           final Set<String> actions, final Set<String> users,
                           final Date from,
                           final Date to) {
        TypedQuery<Long> query = entityManager.createQuery(
                buildCountListCriteria(
                        auditTargets,
                        actions,
                        users,
                        from,
                        to));
        return query.getSingleResult();
    }

    private CriteriaQuery<Long> buildCountListCriteria(final Set<String> auditTargets,
                                                       final Set<String> actions,
                                                       final Set<String> users,
                                                       final Date from,
                                                       final Date to) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Audit> root = criteriaQuery.from(Audit.class);
        criteriaQuery.select(criteriaBuilder.count(root));
        where(auditTargets, actions, users, from, to, criteriaBuilder, criteriaQuery, root);
        return criteriaQuery;
    }

    private void where(final Set<String> auditTargets,
                       final Set<String> actions,
                       final Set<String> users,
                       final Date from,
                       final Date to,
                       final CriteriaBuilder criteriaBuilder,
                       final CriteriaQuery criteriaQuery,
                       final Root<Audit> root) {

        List<Predicate> predicates = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(actions)) {
            Path<Object> actionField = root.get("action");
            predicates.add(actionField.in(actions));
        }
        if (CollectionUtils.isNotEmpty(auditTargets)) {
            Path<Object> auditTargetField = root.get("id").get("auditTargetName");
            predicates.add(auditTargetField.in(auditTargets));
        }
        if (CollectionUtils.isNotEmpty(users)) {
            Path<Object> userField = root.get("user");
            predicates.add(userField.in(users));
        }
        if (from != null) {
            Path<Date> changedDate = root.get("changed");
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(changedDate, from));
        }
        if (to != null) {
            Path<Date> changedDate = root.get("changed");
            predicates.add(criteriaBuilder.lessThanOrEqualTo(changedDate, to));
        }
        if (!predicates.isEmpty()) {
            criteriaQuery.where(predicates.toArray(new Predicate[]{}));
        }
    }

    @Override
    public void saveMessageAudit(final MessageAudit messageAudit) {
        entityManager.persist(messageAudit);
    }

    @Override
    public void saveJmsMessageAudit(final JmsMessageAudit jmsMessageAudit) {
        entityManager.persist(jmsMessageAudit);
    }
}
