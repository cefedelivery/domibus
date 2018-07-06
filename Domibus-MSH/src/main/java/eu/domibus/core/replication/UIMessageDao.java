package eu.domibus.core.replication;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.model.MessageSubtype;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Component;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.0
 */
@Component
public class UIMessageDao extends BasicDao<UIMessageEntity> {

    /**
     * logger
     */
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIMessageDao.class);

    private static final String MESSAGE_ID = "MESSAGE_ID";


    public UIMessageDao() {
        super(UIMessageEntity.class);
    }

    /**
     * find {@link UIMessageEntity} by messageId
     *
     * @param messageId
     * @return an instance of {@link UIMessageEntity}
     */
    public UIMessageEntity findUIMessageByMessageId(final String messageId) {

        final TypedQuery<UIMessageEntity> query = this.em.createNamedQuery("UIMessageEntity.findUIMessageByMessageId", UIMessageEntity.class);
        query.setParameter(MESSAGE_ID, messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }

    /**
     * Counts the messages from {@code TB_MESSAGE_UI} table
     * filter object should contain messageType
     *
     * @param filters it should include messageType always - User or Signal message
     * @return number of messages
     */
    public int countMessages(Map<String, Object> filters) {

        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<UIMessageEntity> uiMessageEntity = cq.from(UIMessageEntity.class);
        cq.select(cb.count(uiMessageEntity));
        List<Predicate> predicates = getPredicates(filters, cb, uiMessageEntity);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<Long> query = em.createQuery(cq);

        long startTime = System.currentTimeMillis();

        Long result = query.getSingleResult();
        if (LOG.isDebugEnabled()) {
            final long endTime = System.currentTimeMillis();
            LOG.debug("[{}] milliseconds to countMessages", endTime - startTime);
        }
        return result.intValue();
    }

    /**
     * list the messages from {@code TB_MESSAGE_UI} table with pagination
     *
     * @param from    the beginning of the page
     * @param max     how many messages in a page
     * @param column  which column to order by
     * @param asc     ordering type - ascending or descending
     * @param filters it should include messageType always - User or Signal message
     * @return a list of {@link UIMessageEntity}
     */
    public List<UIMessageEntity> findPaged(int from, int max, String column, boolean asc, Map<String, Object> filters) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<UIMessageEntity> cq = cb.createQuery(UIMessageEntity.class);
        Root<UIMessageEntity> ume = cq.from(UIMessageEntity.class);
        cq.select(ume);
        List<Predicate> predicates = getPredicates(filters, cb, ume);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (column != null) {
            if (asc) {
                cq.orderBy(cb.asc(ume.get(column)));
            } else {
                cq.orderBy(cb.desc(ume.get(column)));
            }

        }
        TypedQuery<UIMessageEntity> query = this.em.createQuery(cq);
        query.setFirstResult(from);
        query.setMaxResults(max);
        return query.getResultList();
    }


    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder cb, Root<? extends UIMessageEntity> ume) {
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                if (filter.getValue() instanceof String) {
                    if (StringUtils.isNotBlank(filter.getKey()) && !filter.getValue().toString().isEmpty()) {
                        predicates.add(cb.equal(ume.get(filter.getKey()), filter.getValue()));
                    }
                } else if (filter.getValue() instanceof Date) {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey()) {
                            case "receivedFrom":
                                predicates.add(cb.greaterThanOrEqualTo(ume.<Date>get("received"), Timestamp.valueOf(filter.getValue().toString())));
                                break;
                            case "receivedTo":
                                predicates.add(cb.lessThanOrEqualTo(ume.<Date>get("received"), Timestamp.valueOf(filter.getValue().toString())));
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    predicates.add(cb.equal(ume.<String>get(filter.getKey()), filter.getValue()));
                }
            } else {
                if (filter.getKey().equals("messageSubtype")) {
                    predicates.add(cb.isNull(ume.<MessageSubtype>get("messageSubtype")));
                }
            }
        }
        return predicates;
    }
}