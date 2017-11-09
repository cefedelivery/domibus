package eu.domibus.common.dao;

import com.google.common.collect.Sets;
import eu.domibus.common.model.audit.Audit;
import eu.domibus.common.model.audit.JmsMessageAudit;
import eu.domibus.common.model.audit.MessageAudit;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.hibernate.internal.SessionFactoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class AuditDaoImplTest {


    @Injectable
    EntityManager entityManager;
    @Tested
    AuditDaoImpl auditDao;

    @Test
    public void whereWithNoArgument(
            @Mocked CriteriaBuilder criteriaBuilder,
            @Mocked CriteriaQuery criteriaQuery,
            @Mocked Root<Audit> root,
            @Mocked Predicate predicate) throws Exception {
        AuditDaoImpl auditDao = new AuditDaoImpl();
        auditDao.where(new HashSet<>(), new HashSet<>(), new HashSet<>(), null, null, criteriaBuilder, criteriaQuery, root);
        new Verifications() {{
            criteriaQuery.where(withAny(predicate));
            times = 0;
        }};
    }

    @Test
    public void whereWithAllParametersTarget(
            @Mocked CriteriaBuilder criteriaBuilder,
            @Mocked SessionFactoryImpl sessionFactory,
            @Mocked CriteriaQuery criteriaQuery,
            @Mocked Root<Audit> root) throws Exception {
        auditDao.where(Sets.newHashSet("User", "Pmode"), Sets.newHashSet("ADD"), Sets.newHashSet("Admin"), new Date(), new Date(), criteriaBuilder, criteriaQuery, root);
        new Verifications() {{
            Predicate[] predicateList;
            criteriaQuery.where(predicateList = withCapture());
            times = 1;
            root.get("id");
            times = 2;
            root.get("id").get("auditTargetName");
            times = 1;
            root.get("id").get("action");
            times = 1;
            root.get("changed");
            times = 2;
            root.get("user");
            times = 1;
            assertEquals(5, predicateList.length);

        }};
    }

    @Test
    public void buildAuditListCriteria(@Mocked CriteriaBuilder criteriaBuilder,
                                       @Mocked CriteriaQuery criteriaQuery,
                                       @Mocked Root<Audit> root) {
        Set<String> auditTarget = Sets.newHashSet("User", "Pmode");
        Set<String> action = Sets.newHashSet("ADD");
        Set<String> user = Sets.newHashSet("Admin");
        Date from = new Date();
        Date to = new Date();
        new Expectations() {{
            entityManager.getCriteriaBuilder();
            result = criteriaBuilder;
            criteriaBuilder.createQuery(Audit.class);
            result = criteriaQuery;
            criteriaQuery.from(Audit.class);
            result = root;
        }};
        auditDao.buildAuditListCriteria(auditTarget, action, user, from, to);
        new Verifications() {{
            criteriaQuery.select(root);
            auditDao.where(auditTarget, action, user, from, to, criteriaBuilder, criteriaQuery, root);
        }};


    }

    @Test
    public void buildAuditCountCriteria(@Mocked CriteriaBuilder criteriaBuilder,
                                        @Mocked CriteriaQuery criteriaQuery,
                                        @Mocked Root<Audit> root,
                                        @Mocked Expression expression) {
        Set<String> auditTarget = Sets.newHashSet("User", "Pmode");
        Set<String> action = Sets.newHashSet("ADD");
        Set<String> user = Sets.newHashSet("Admin");
        Date from = new Date();
        Date to = new Date();
        new Expectations() {{
            entityManager.getCriteriaBuilder();
            result = criteriaBuilder;
            criteriaBuilder.createQuery(Long.class);
            result = criteriaQuery;
            criteriaQuery.from(Audit.class);
            result = root;
            criteriaBuilder.count(root);
            result = expression;
        }};
        auditDao.buildAuditCountCriteria(auditTarget, action, user, from, to);
        new Verifications() {{
            criteriaQuery.select(expression);
            times = 1;
            auditDao.where(auditTarget, action, user, from, to, criteriaBuilder, criteriaQuery, root);
            times = 1;
        }};
    }

    @Test
    public void listAudit(@Mocked CriteriaQuery criteriaQuery, @Mocked TypedQuery<Audit> query) {
        new Expectations() {{
            entityManager.createQuery(withAny(criteriaQuery));
        }};
        auditDao.listAudit(Sets.newHashSet("User", "Pmode"), Sets.newHashSet("ADD"), Sets.newHashSet("Admin"), new Date(), new Date(), 0, 10);
        new Verifications() {{
            query.setFirstResult(0);
            times = 1;
            query.setMaxResults(10);
            times = 1;
            query.getResultList();
            times = 1;
        }};
    }

    @Test
    public void countAudit(@Mocked CriteriaQuery criteriaQuery, @Mocked TypedQuery<Long> query) {
        new Expectations() {{
            entityManager.createQuery(withAny(criteriaQuery));
        }};
        auditDao.countAudit(Sets.newHashSet("User", "Pmode"), Sets.newHashSet("ADD"), Sets.newHashSet("Admin"), new Date(), new Date());
        new Verifications() {{
            query.getSingleResult();
            times = 1;
            query.setFirstResult(0);
            times = 0;
            query.setMaxResults(10);
            times = 0;
        }};
    }

    @Test
    public void saveMessageAudit() {
        MessageAudit messageAudit = new MessageAudit();
        auditDao.saveMessageAudit(messageAudit);
        new Verifications() {{
            entityManager.persist(messageAudit);
            times = 1;
        }};
    }

    @Test
    public void saveJmsMessageAudit() {
        JmsMessageAudit messageAudit = new JmsMessageAudit();
        auditDao.saveJmsMessageAudit(messageAudit);
        new Verifications() {{
            entityManager.persist(messageAudit);
            times = 1;
        }};
    }

}