package eu.domibus.common.dao;

import com.google.common.collect.Maps;
import eu.domibus.common.MSHRole;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.model.logging.UserMessageLogInfoFilter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */
@Repository
public class UserMessageLogDao extends MessageLogDao<UserMessageLog> {

    @Autowired
    private UserMessageLogInfoFilter userMessageLogInfoFilter;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLogDao.class);

    public UserMessageLogDao() {
        super(UserMessageLog.class);
    }

    public List<String> findRetryMessages() {
        TypedQuery<String> query = this.em.createNamedQuery("UserMessageLog.findRetryMessages", String.class);

        return query.getResultList();
    }

    public List<String> findPullWaitingForReceiptMessages() {
        TypedQuery<String> query = this.em.createNamedQuery("UserMessageLog.findPullWaitingForReceiptMessages", String.class);
        return query.getResultList();
    }

    public List<String> findTimedoutMessages(int timeoutTolerance) {
        TypedQuery<String> query = this.em.createNamedQuery("UserMessageLog.findTimedoutMessages", String.class);
        query.setParameter("TIMESTAMP_WITH_TOLERANCE", new Date(System.currentTimeMillis() - timeoutTolerance));

        return query.getResultList();
    }

    public List<String> findTimedOutPullMessages(int timeoutTolerance) {
        TypedQuery<String> query = this.em.createNamedQuery("UserMessageLog.findPullTimedoutMessages", String.class);
        query.setParameter("TIMESTAMP_WITH_TOLERANCE", new Date(System.currentTimeMillis() - timeoutTolerance));
        return query.getResultList();
    }

    public List<String> findFailedMessages(String finalRecipient) {
        return findFailedMessages(finalRecipient, null, null);
    }

    public List<String> findFailedMessages(String finalRecipient, Date failedStartDate, Date failedEndDate) {
        String queryString = "select distinct m.messageInfo.messageId from UserMessage m " +
                "inner join m.messageProperties.property p, UserMessageLog ml " +
                "where ml.messageId = m.messageInfo.messageId and ml.messageStatus = 'SEND_FAILURE' and ml.messageType = 'USER_MESSAGE' and ml.deleted is null ";
        if (StringUtils.isNotEmpty(finalRecipient)) {
            queryString += " and p.name = 'finalRecipient' and p.value = :FINAL_RECIPIENT";
        }
        if (failedStartDate != null) {
            queryString += " and ml.failed >= :START_DATE";
        }
        if (failedEndDate != null) {
            queryString += " and ml.failed <= :END_DATE";
        }
        TypedQuery<String> query = this.em.createQuery(queryString, String.class);
        if (StringUtils.isNotEmpty(finalRecipient)) {
            query.setParameter("FINAL_RECIPIENT", finalRecipient);
        }
        if (failedStartDate != null) {
            query.setParameter("START_DATE", failedStartDate);
        }
        if (failedEndDate != null) {
            query.setParameter("END_DATE", failedEndDate);
        }
        return query.getResultList();
    }

    public UserMessageLog findByMessageId(String messageId) {
        TypedQuery<UserMessageLog> query = em.createNamedQuery("UserMessageLog.findByMessageId", UserMessageLog.class);
        query.setParameter("MESSAGE_ID", messageId);
        return query.getSingleResult();
    }

    public UserMessageLog findByMessageId(String messageId, MSHRole mshRole) {
        TypedQuery<UserMessageLog> query = this.em.createNamedQuery("UserMessageLog.findByMessageIdAndRole", UserMessageLog.class);
        query.setParameter("MESSAGE_ID", messageId);
        query.setParameter("MSH_ROLE", mshRole);

        try {
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("Query UserMessageLog.findByMessageId did not find any result for message with id [" + messageId + "] and MSH role [" + mshRole + "]");
            return null;
        }
    }

    public Long countMessages(HashMap<String, Object> filters) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<UserMessageLog> mle = cq.from(UserMessageLog.class);
        cq.select(cb.count(mle));
        List<Predicate> predicates = getPredicates(filters, cb, mle);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<Long> query = em.createQuery(cq);
        return query.getSingleResult();
    }

    public List<UserMessageLog> findPaged(int from, int max, String column, boolean asc, HashMap<String, Object> filters) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<UserMessageLog> cq = cb.createQuery(UserMessageLog.class);
        Root<UserMessageLog> mle = cq.from(UserMessageLog.class);
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
        TypedQuery<UserMessageLog> query = this.em.createQuery(cq);
        query.setFirstResult(from);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public List<String> getUndownloadedUserMessagesOlderThan(Date date, String mpc) {
        TypedQuery<String> query = em.createNamedQuery("UserMessageLog.findUndownloadedUserMessagesOlderThan", String.class);
        query.setParameter("DATE", date);
        query.setParameter("MPC", mpc);
        try {
            return query.getResultList();
        } catch (NoResultException nrEx) {
            LOG.debug("Query UserMessageLog.findUndownloadedUserMessagesOlderThan did not find any result for date [" + date + "] and MPC [" + mpc + "]", nrEx);
            return Collections.EMPTY_LIST;
        }
    }

    public List<String> getDownloadedUserMessagesOlderThan(Date date, String mpc) {
        TypedQuery<String> query = em.createNamedQuery("UserMessageLog.findDownloadedUserMessagesOlderThan", String.class);
        query.setParameter("DATE", date);
        query.setParameter("MPC", mpc);
        try {
            return query.getResultList();
        } catch (NoResultException nrEx) {
            LOG.debug("Query UserMessageLog.findDownloadedUserMessagesOlderThan did not find any result for date [" + date + "] and MPC [" + mpc + "]", nrEx);
            return Collections.EMPTY_LIST;
        }
    }

    public String findBackendForMessageId(String messageId) {
        TypedQuery<String> query = em.createNamedQuery("UserMessageLog.findBackendForMessage", String.class);
        query.setParameter("MESSAGE_ID", messageId);
        return query.getSingleResult();
    }

    public void setAsNotified(String messageId) {
        final UserMessageLog messageLog = findByMessageId(messageId);
        messageLog.setNotificationStatus(NotificationStatus.NOTIFIED);
        super.update(messageLog);
    }

    public int countAllInfo(boolean asc, HashMap<String, Object> filters) {
        LOG.debug("Count all");
        final Map<String, Object> filteredEntries = Maps.filterEntries(filters, new com.google.common.base.Predicate<Map.Entry<String, Object>>() {
            @Override
            public boolean apply(Map.Entry<String, Object> input) {
                return input.getValue() != null;
            }
        });
        if (filteredEntries.size() == 0) {
            LOG.debug("Filter empty");
            return countAll();
        }
        String filteredSignalMessageLogQuery = userMessageLogInfoFilter.countUserMessageLogQuery(asc, filters);
        TypedQuery<Number> countQuery = em.createQuery(filteredSignalMessageLogQuery, Number.class);
        countQuery = userMessageLogInfoFilter.applyParameters(countQuery, filters);
        final Number count = countQuery.getSingleResult();
        return count.intValue();
    }

    public Integer countAll() {
        LOG.debug("Executing native query");
        final Query nativeQuery = em.createNativeQuery("SELECT count(um.ID_PK) FROM  TB_USER_MESSAGE um");
        final Number singleResult = (Number) nativeQuery.getSingleResult();
        return singleResult.intValue();
    }

    public List<MessageLogInfo> findAllInfoPaged(int from, int max, String column, boolean asc, HashMap<String, Object> filters) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving messages for parameters from " + from + " max " + max + " column " + column + " asc " + asc);
            for (Map.Entry<String, Object> stringObjectEntry : filters.entrySet()) {
                if (stringObjectEntry.getValue() != null) {
                    LOG.debug("Setting parameters for query ");
                    LOG.debug(stringObjectEntry.getKey() + "  " + stringObjectEntry.getValue());
                }
            }
        }

        String filteredUserMessageLogQuery = userMessageLogInfoFilter.filterUserMessageLogQuery(column, asc, filters);
        TypedQuery<MessageLogInfo> typedQuery = em.createQuery(filteredUserMessageLogQuery, MessageLogInfo.class);
        TypedQuery<MessageLogInfo> queryParameterized = userMessageLogInfoFilter.applyParameters(typedQuery, filters);
        queryParameterized.setFirstResult(from);
        queryParameterized.setMaxResults(max);
        long startTime = 0;
        if (LOG.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }
        final List<MessageLogInfo> resultList = queryParameterized.getResultList();
        if (LOG.isDebugEnabled()) {
            final long endTime = System.currentTimeMillis();
            LOG.debug(endTime - startTime + "milliscond to execute query for " + resultList.size() + " resuts");
        }
        return resultList;
    }


}
