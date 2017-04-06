package eu.domibus.common.dao;

import eu.domibus.common.MSHRole;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.model.logging.UserMessageLogInfo;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */
@Repository
public class UserMessageLogDao extends MessageLogDao<UserMessageLog> {

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

    public List<UserMessageLogInfo> findAllInfoPaged(int from, int max, String column, boolean asc, HashMap<String, Object> filters) {
        /*String userMessageLogQuery = "select new eu.domibus.common.model.logging.UserMessageLogInfo(log, message.collaborationInfo.conversationId, partyFrom.value, partyTo.value, propsFrom.value, propsTo.value, info.refToMessageId) from UserMessageLog log, " +
                "UserMessage message " +
                "left join log.messageInfo info " +
                "left join message.messageProperties.property propsFrom " +
                "left join message.messageProperties.property propsTo " +
                "left join message.partyInfo.from.partyId partyFrom " +
                "left join message.partyInfo.to.partyId partyTo " +
                "where message.messageInfo = info and propsFrom.name = 'originalSender'" +
                "and propsTo.name = 'finalRecipient'";

        TypedQuery<UserMessageLogInfo> typedQuery = em.createQuery(userMessageLogQuery, UserMessageLogInfo.class);
        return typedQuery.getResultList();*/

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<UserMessageLogInfo> criteriaQuery = criteriaBuilder.createQuery(UserMessageLogInfo.class);
        Root<UserMessageLog> userMessageLogRoot = criteriaQuery.from(UserMessageLog.class);
        Root<UserMessage> userMessageRoot = criteriaQuery.from(UserMessage.class);
        //Join<UserMessage, MessageProperties> propsFromJoin = userMessageRoot.join("messageProperties");
        //Join<UserMessage,PartyId> fromPartyJoin = userMessageRoot.join("partyInfo").join("from");
        //Fetch<UserMessage, PartyId> partyInfo = userMessageRoot.fetch("partyInfo");
        Join<UserMessage, PartyInfo> partyInfoJoin = userMessageRoot.join("partyInfo");
        Join<UserMessageLog, MessageInfo> messageInfoJoin = userMessageLogRoot.join("messageInfo");

        Predicate messageInfoPredicate = criteriaBuilder.equal(userMessageRoot.get("messageInfo"), messageInfoJoin);
        //Predicate propsFromPredicate = criteriaBuilder.equal(propsFromJoin.get("property").get("name"),"originalSender");

        criteriaQuery.select(criteriaBuilder.construct(UserMessageLogInfo.class,
                messageInfoJoin.getParent(),
                userMessageRoot.get("collaborationInfo").get("conversationId"),
                //propsFromJoin.get("value"),
                userMessageRoot.get("messageInfo").get("refToMessageId")
                )
        ).distinct(true);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(messageInfoPredicate);
        predicates.addAll(getPredicatesLogInfo(filters, criteriaBuilder, userMessageLogRoot, userMessageRoot));

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));

        TypedQuery<UserMessageLogInfo> query = this.em.createQuery(criteriaQuery);
        query.setFirstResult(from);
        query.setMaxResults(max);
        return query.getResultList();
    }

}
