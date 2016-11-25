package eu.domibus.common.dao;

import eu.domibus.common.MSHRole;
import eu.domibus.common.model.logging.SignalMessageLog;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Repository
public class SignalMessageLogDao extends MessageLogDao<SignalMessageLog> {

    private static final Log LOG = LogFactory.getLog(SignalMessageLogDao.class);

    public SignalMessageLogDao() {
        super(SignalMessageLog.class);
    }

    public SignalMessageLog findByMessageId(String messageId) {
        TypedQuery<SignalMessageLog> query = em.createNamedQuery("SignalMessageLog.findByMessageId", SignalMessageLog.class);
        query.setParameter("MESSAGE_ID", messageId);

        SignalMessageLog signalMessageLog = query.getSingleResult();
        if (!StringUtils.equals(messageId, signalMessageLog.getMessageId())) {
            //Simulating the behavior of TypedQuery.getSingleResult() when no data is found so that existing code does not break
            throw new NoResultException("The message id queried :" + messageId + " failed in case sensitive match check with database!");
        }
        return signalMessageLog;
    }

    public SignalMessageLog findByMessageId(String messageId, MSHRole mshRole) {
        TypedQuery<SignalMessageLog> query = em.createNamedQuery("SignalMessageLog.findByMessageIdAndRole", SignalMessageLog.class);
        query.setParameter("MESSAGE_ID", messageId);
        query.setParameter("MSH_ROLE", mshRole);

        SignalMessageLog signalMessageLog;
        try {
            signalMessageLog = query.getSingleResult();
            if (!StringUtils.equals(messageId, signalMessageLog.getMessageId())) {
                //Simulating the behavior of TypedQuery.getSingleResult() when no data is found so that existing code does not break
                throw new NoResultException("The message id queried :" + messageId + " failed in case sensitive match check with database!");
            }
        } catch (NoResultException nrEx) {
            LOG.debug("Query SignalMessageLog.findByMessageId did not find any result for message with id [" + messageId + "] and MSH role [" + mshRole + "]", nrEx);
            signalMessageLog = null;
        }
        return signalMessageLog;
    }

    public Long countMessages(HashMap<String, Object> filters) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<SignalMessageLog> mle = cq.from(SignalMessageLog.class);
        cq.select(cb.count(mle));
        List<Predicate> predicates = getPredicates(filters, cb, mle);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<Long> query = em.createQuery(cq);
        return query.getSingleResult();
    }

    public List<SignalMessageLog> findPaged(int from, int max, String column, boolean asc, HashMap<String, Object> filters) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<SignalMessageLog> cq = cb.createQuery(SignalMessageLog.class);
        Root<SignalMessageLog> mle = cq.from(SignalMessageLog.class);
        cq.select(mle);
        List<Predicate> predicates = getPredicates(filters, cb, mle);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (column != null) {
            if (asc) {
                cq.orderBy(cb.asc(mle.get(column)));
            } else {
                cq.orderBy(cb.desc(mle.get(column)));
            }

        }
        TypedQuery<SignalMessageLog> query = this.em.createQuery(cq);
        query.setFirstResult(from);
        query.setMaxResults(max);
        return query.getResultList();
    }

}