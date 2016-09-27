package eu.domibus.common.dao;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.logging.MessageLog;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.*;

/**
 * @param <F>
 * @author Federico Martini
 * @since 3.2
 */
public abstract class MessageLogDao<F extends MessageLog> extends BasicDao {

    public <F extends MessageLog> MessageLogDao(final Class<F> type) {

        super(type);
    }

    public void setMessageAsAck(String messageId) {
        this.setMessageStatus(messageId, MessageStatus.ACKNOWLEDGED);
    }

    public void setMessageAsAckWithWarnings(String messageId) {
        this.setMessageStatus(messageId, MessageStatus.ACKNOWLEDGED_WITH_WARNING);
    }

    public void setMessageAsWaitingForReceipt(String messageId) {
        this.setMessageStatus(messageId, MessageStatus.WAITING_FOR_RECEIPT);
    }

    protected abstract void setMessageStatus(String messageId, MessageStatus messageStatus);

    public abstract MessageStatus getMessageStatus(String messageId);

    public abstract MessageLog findByMessageId(String messageId, MSHRole mshRole);

    public Long countMessages(HashMap<String, Object> filters) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MessageLog> mle = cq.from(MessageLog.class);
        cq.select(cb.count(mle));
        List<Predicate> predicates = getPredicates(filters, cb, mle);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<Long> query = em.createQuery(cq);
        return query.getSingleResult();
    }

    public List<MessageLog> findPaged(int from, int max, String column, boolean asc, HashMap<String, Object> filters) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<MessageLog> cq = cb.createQuery(MessageLog.class);
        Root<MessageLog> mle = cq.from(MessageLog.class);
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
        TypedQuery<MessageLog> query = this.em.createQuery(cq);
        query.setFirstResult(from);
        query.setMaxResults(max);
        return query.getResultList();
    }

    protected List<Predicate> getPredicates(HashMap<String, Object> filters, CriteriaBuilder cb, Root<MessageLog> mle) {
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                if (filter.getValue() instanceof String) {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey().toString()) {
                            case "receivedFrom":
                                predicates.add(cb.greaterThanOrEqualTo(mle.<Date>get("received"), Timestamp.valueOf(filter.getValue().toString())));
                                break;
                            case "receivedTo":
                                predicates.add(cb.lessThanOrEqualTo(mle.<Date>get("received"), Timestamp.valueOf(filter.getValue().toString())));
                                break;
                            default:
                                predicates.add(cb.like(mle.<String>get(filter.getKey()), (String) filter.getValue()));
                                break;
                        }
                    }
                } else {
                    predicates.add(cb.equal(mle.<String>get(filter.getKey()), filter.getValue()));
                }
            }
        }
        return predicates;
    }


}