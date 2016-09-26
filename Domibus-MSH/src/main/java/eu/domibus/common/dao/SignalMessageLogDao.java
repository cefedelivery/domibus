package eu.domibus.common.dao;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.logging.SignalMessageLog;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Repository
@Transactional
public class SignalMessageLogDao extends MessageLogDao<SignalMessageLog> {

    public SignalMessageLogDao() {
        super(SignalMessageLog.class);
    }

    public List<String> findRetryMessages() {
        TypedQuery<String> query = this.em.createNamedQuery("SignalMessageLog.findRetryMessages", String.class);

        return query.getResultList();
    }

    public List<String> findTimedoutMessages(int timeoutTolerance) {
        TypedQuery<String> query = this.em.createNamedQuery("SignalMessageLog.findTimedoutMessages", String.class);
        query.setParameter("TIMESTAMP_WITH_TOLERANCE", new Date(System.currentTimeMillis() - timeoutTolerance));

        return query.getResultList();
    }


    protected void setMessageStatus(String messageId, MessageStatus messageStatus) {

        Query query = this.em.createNamedQuery("SignalMessageLog.setMessageStatus");
        query.setParameter("MESSAGE_ID", messageId);
        query.setParameter("TIMESTAMP", new Date());
        query.setParameter("MESSAGE_STATUS", messageStatus);
        int result = query.executeUpdate();
        if (result != 1) {
            this.em.getTransaction().setRollbackOnly();
            BasicDao.LOG.error("Could not set message " + messageId + " as " + messageStatus);
        }
    }

    public MessageStatus getMessageStatus(String messageId) {

        TypedQuery<MessageStatus> query = this.em.createNamedQuery("SignalMessageLog.getMessageStatus", MessageStatus.class);
        query.setParameter("MESSAGE_ID", messageId);
        try {
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            BasicDao.LOG.debug("Query SignalMessageLog.getMessageStatus did not find any result for message with id [" + messageId + "]", nrEx);
            return MessageStatus.NOT_FOUND;
        }
    }

    public SignalMessageLog findByMessageId(String messageId, MSHRole mshRole) {
        TypedQuery<SignalMessageLog> query = this.em.createNamedQuery("SignalMessageLog.findByMessageId", SignalMessageLog.class);
        query.setParameter("MESSAGE_ID", messageId);
        query.setParameter("MSH_ROLE", mshRole);

        try {
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            BasicDao.LOG.debug("Query SignalMessageLog.findByMessageId did not find any result for message with id [" + messageId + "] and MSH role [" + mshRole + "]", nrEx);
            return null;
        }
    }

    /* public Long countMessages(HashMap<String, Object> filters) {
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
 */
    public List<SignalMessageLog> findAll() {
        TypedQuery<SignalMessageLog> query = this.em.createNamedQuery("SignalMessageLog.findEntries", SignalMessageLog.class);
        return query.getResultList();
    }

    public long countEntries() {
        TypedQuery<Long> query = this.em.createNamedQuery("SignalMessageLog.countEntries", Long.class);
        return query.getSingleResult();
    }

    public List<String> getUndownloadedUserMessagesOlderThan(Date date, String mpc) {
        TypedQuery<String> query = em.createNamedQuery("SignalMessageLog.findUndownloadedUserMessagesOlderThan", String.class);
        query.setParameter("DATE", date);
        query.setParameter("MPC", mpc);
        try {
            return query.getResultList();
        } catch (NoResultException nrEx) {
            BasicDao.LOG.debug("Query SignalMessageLog.findUndownloadedUserMessagesOlderThan did not find any result for date [" + date + "] and MPC [" + mpc + "]", nrEx);
            return Collections.EMPTY_LIST;
        }
    }

    public List<String> getDownloadedUserMessagesOlderThan(Date date, String mpc) {
        TypedQuery<String> query = em.createNamedQuery("SignalMessageLog.findDownloadedUserMessagesOlderThan", String.class);
        query.setParameter("DATE", date);
        query.setParameter("MPC", mpc);
        try {
            return query.getResultList();
        } catch (NoResultException nrEx) {
            BasicDao.LOG.warn("Query SignalMessageLog.findDownloadedUserMessagesOlderThan did not find any result for date [" + date + "] and MPC [" + mpc + "]", nrEx);
            return Collections.EMPTY_LIST;
        }
    }

    public String findEndpointForMessageId(String messageId) {
        TypedQuery<String> query = em.createNamedQuery("SignalMessageLog.findEndpointForId", String.class);
        query.setParameter("MESSAGE_ID", messageId);
        return query.getSingleResult();
    }


    public String findBackendForMessageId(String messageId) {
        TypedQuery<String> query = em.createNamedQuery("SignalMessageLog.findBackendForMessage", String.class);
        query.setParameter("MESSAGE_ID", messageId);
        return query.getSingleResult();
    }

    public void setAsNotified(String messageId) {
        Query query = em.createNamedQuery("SignalMessageLog.setNotificationStatus");
        query.setParameter("MESSAGE_ID", messageId);
        query.setParameter("NOTIFICATION_STATUS", NotificationStatus.NOTIFIED);
        query.executeUpdate();
    }
}