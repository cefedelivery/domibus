package eu.domibus.common.dao;

import eu.domibus.common.MSHRole;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.model.logging.UserMessageLogInfo;
import eu.domibus.common.model.logging.UserMessageLogInfoFilter;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.sql.Timestamp;
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

    public List<String> findTimedoutMessages(int timeoutTolerance) {
        TypedQuery<String> query = this.em.createNamedQuery("UserMessageLog.findTimedoutMessages", String.class);
        query.setParameter("TIMESTAMP_WITH_TOLERANCE", new Date(System.currentTimeMillis() - timeoutTolerance));

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
            LOG.debug("Query UserMessageLog.findByMessageId did not find any result for message with id [" + messageId + "] and MSH role [" + mshRole + "]", nrEx);
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

    public String findEndpointForMessageId(String messageId) {
        return findByMessageId(messageId).getEndpoint();
    }

    public String findBackendForMessageId(String messageId) {
        TypedQuery<String> query = em.createNamedQuery("UserMessageLog.findBackendForMessage", String.class);
        query.setParameter("MESSAGE_ID", messageId);
        return query.getSingleResult();
    }

    public void setAsNotified(String messageId) {
        Query query = em.createNamedQuery("UserMessageLog.setNotificationStatus");
        query.setParameter("MESSAGE_ID", messageId);
        query.setParameter("NOTIFICATION_STATUS", NotificationStatus.NOTIFIED);
        query.executeUpdate();
    }

    /*String filterUserMessageLogQuery(String query, String column, boolean asc, HashMap<String, Object> filters) {
        StringBuilder result = new StringBuilder(query);
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                result.append(" and ");
                if (!(filter.getValue() instanceof Date)) {
                    if (!filter.getValue().toString().isEmpty()) {
                        String tableName = "";
                        switch (filter.getKey()) {
                            case "messageId":
                            case "mshRole":
                            case "messageType":
                            case "messageStatus":
                            case "notificationStatus":
                                result.append("log").append(".").append(filter.getKey()).append(" = '").append(filter.getValue()).append("'");
                                break;
                            case "fromPartyId":
                                result.append("partyFrom.value").append(" = '").append(filter.getValue()).append("'");
                                break;
                            case "toPartyId":
                                result.append("partyTo.value").append(" = '").append(filter.getValue()).append("'");
                                break;
                            case "refToMessageId":
                                result.append("info.refToMessageId").append(" = '").append(filter.getValue()).append("'");
                                break;
                            case "originalSender":
                                result.append("propsFrom.value").append(" = '").append(filter.getValue()).append("'");
                                break;
                            case "finalRecipient":
                                result.append("propsTo.value").append(" = '").append(filter.getValue()).append("'");
                                break;
                            case "conversationId":
                                result.append("message.collaborationInfo").append(".").append(filter.getKey()).append(" = '").append(filter.getValue()).append("'");
                            default:
                                break;
                        }
                    }
                } else {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey()) {
                            case "":
                                break;
                            case "receivedFrom":
                                result.append("log.received").append(" >= '").append(filter.getValue()).append("'");
                                break;
                            case "receivedTo":
                                result.append("log.received").append(" <= '").append(filter.getValue()).append("'");
                                break;
                        }
                    }
                }
            }
        }

        if (column != null) {
            String usedColumn = null;
            switch(column) {
                case "messageId":
                case "mshRole":
                case "messageType":
                case "messageStatus":
                case "notificationStatus":
                case "deleted":
                case "received":
                default:
                    usedColumn = "log." + column;
                    break;
                case "fromPartyId":
                    usedColumn = "partyFrom.value";
                    break;
                case "toPartyId":
                    usedColumn = "partyTo.value";
                    break;
                case "refToMessageId":
                    usedColumn = "info.refToMessageId";
                    break;
                case "originalSender":
                    usedColumn = "propsFrom.value";
                    break;
                case "finalRecipient":
                    usedColumn = "propsTo.value";
                    break;
                case "conversationId":
                    usedColumn = "message.collaborationInfo"+column;
                    break;
            }
            if (asc) {
                result.append(" order by ").append(usedColumn).append(" asc");
            } else {
                result.append(" order by ").append(usedColumn).append(" desc");
            }
        }

        return result.toString();
    }*/

    public List<UserMessageLogInfo> findAllInfoPaged(int from, int max, String column, boolean asc, HashMap<String, Object> filters) {
        String filteredUserMessageLogQuery = userMessageLogInfoFilter.filterUserMessageLogQuery(column, asc, filters);
        TypedQuery<UserMessageLogInfo> typedQuery = em.createQuery(filteredUserMessageLogQuery, UserMessageLogInfo.class);
        TypedQuery<UserMessageLogInfo> queryParameterized = userMessageLogInfoFilter.applyParameters(typedQuery, filters);
        queryParameterized.setFirstResult(from);
        queryParameterized.setMaxResults(max);
        return queryParameterized.getResultList();
    }

}
