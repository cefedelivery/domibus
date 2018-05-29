package eu.domibus.core.pull;

import eu.domibus.api.configuration.DataBaseEngine;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.core.pull.PullMessageState.EXPIRED;
import static eu.domibus.core.pull.PullMessageState.RETRY;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
@Repository
public class MessagingLockDaoImpl implements MessagingLockDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockDaoImpl.class);

    private static final String MESSAGE_ID = "MESSAGE_ID";

    protected static final String ID_PK_PARAM = "idPk";
    public static final String MESSAGE_STALED_COLUM = "MESSAGE_STALED";
    public static final String ID_PK_COLUMN = "ID_PK";
    public static final String DEL = "DEL";
    public static final String READY = "READY";
    public static final String PROCESS = "PROCESS";
    public static final String MESSAGE_STATE = "MESSAGE_STATE";

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    private final static Map<DataBaseEngine, String> lockByIdQuery = new HashMap<>();

    private final static Map<DataBaseEngine, String> lockByMessageIdQuery = new HashMap<>();

    private final static Map<DataBaseEngine, String> unlockByMessageIdQuery = new HashMap<>();

    static {
        lockByIdQuery.put(DataBaseEngine.MYSQL, "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED  FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=?1 FOR UPDATE");
        lockByIdQuery.put(DataBaseEngine.H2, "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=?1 FOR UPDATE");
        lockByIdQuery.put(DataBaseEngine.ORACLE, "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=?1 FOR UPDATE NOWAIT");

        lockByMessageIdQuery.put(DataBaseEngine.MYSQL, "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_ID=?1 FOR UPDATE");
        lockByMessageIdQuery.put(DataBaseEngine.H2, "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_ID=?1 FOR UPDATE");
        lockByMessageIdQuery.put(DataBaseEngine.ORACLE, "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_ID=?1 FOR UPDATE NOWAIT");

        unlockByMessageIdQuery.put(DataBaseEngine.MYSQL, "SELECT ml.ID_PK,ml.MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='PROCESS' and ml.MESSAGE_ID=:MESSAGE_ID FOR UPDATE");
        unlockByMessageIdQuery.put(DataBaseEngine.H2, "SELECT ml.ID_PK,ml.MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='PROCESS' and ml.MESSAGE_ID=:MESSAGE_ID FOR UPDATE");
        unlockByMessageIdQuery.put(DataBaseEngine.ORACLE, "SELECT ml.ID_PK,ml.MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='PROCESS' and ml.MESSAGE_ID=:MESSAGE_ID FOR UPDATE  NOWAIT");

    }


    @Autowired
    @Qualifier("domibusJDBC-XADataSource")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional
    public PullMessageId getNextPullMessageToProcess(final Integer idPk) {

        Query q = entityManager.createNativeQuery(lockByIdQuery.get(domibusConfigurationService.getDataBaseEngine()), MessagingLock.class);
        q.setParameter(1, idPk);
        final List<MessagingLock> resultList = q.getResultList();
        for (MessagingLock messagingLock : resultList) {
            try {
                final String messageId = messagingLock.getMessageId();
                final java.util.Date messageStaled = messagingLock.getStaled();
                final int sendAttempts = messagingLock.getSendAttempts();
                final int sendAttemptsMax = messagingLock.getSendAttemptsMax();
                if (messageStaled.compareTo(new Date(System.currentTimeMillis())) < 0) {
                    messagingLock.setMessageState(MessageState.DEL);
                    entityManager.persist(messagingLock);
                    return new PullMessageId(messageId, EXPIRED, String.format("Maximum time to send the message has been reached:[%tc]", messageStaled));
                }
                if (sendAttempts >= sendAttemptsMax) {
                    messagingLock.setMessageState(MessageState.DEL);
                    entityManager.persist(messagingLock);
                    return new PullMessageId(messageId, EXPIRED, String.format("Maximum number of attempts to send the message has been reached:[%d]", sendAttempts));
                }
                if (sendAttempts >= 0) {
                    messagingLock.setMessageState(MessageState.PROCESS);
                    entityManager.persist(messagingLock);
                }
                if (sendAttempts > 0) {
                    return new PullMessageId(messageId, RETRY);
                }
                LOG.debug("Message[{}] locked", messageId);
                return new PullMessageId(messageId);
            } catch (org.hibernate.exception.LockAcquisitionException ex) {
                LOG.trace("Message lock[{}] was alredy locked.", idPk, ex);
            }
        }

        return null;

    }

    //this method is needed because the oracle jdbc driver does return an oracle.sql.TIMESTAMP which guess what...
    //does not extends java.sql.Timestamp.
    private Timestamp getTimestamp(Object object) {
        final String className = object.getClass().getName();

        if (DataBaseEngine.ORACLE == domibusConfigurationService.getDataBaseEngine()) {
            if ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className)) {
                try {
                    final Method timestampValueMethod = object.getClass().getMethod("timestampValue");
                    return (Timestamp) timestampValueMethod.invoke(object);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    LOG.error("Impossible to retrieve oracle timestamp");
                    throw new IllegalStateException("Impossible to retrieve oracle timestamp");
                }
            }
        }
        return (Timestamp) object;
    }


    @Override
    public void releaseLock(final MessagingLock messagingLock) {
        messagingLock.setMessageState(MessageState.READY);
        entityManager.merge(messagingLock);
    }

    @Override
    public void readyToDelete(final String messageId) {
        final MessagingLock messagingLockForMessageId = findMessagingLockForMessageId(messageId);
        messagingLockForMessageId.setMessageState(MessageState.DEL);
        entityManager.persist(messagingLockForMessageId);
    }

    @Override
    public MessagingLock getLock(final String messageId) {

        try {
            Query q = entityManager.createNativeQuery(lockByMessageIdQuery.get(domibusConfigurationService.getDataBaseEngine()), MessagingLock.class);
            q.setParameter(1, messageId);
            return (MessagingLock) q.getSingleResult();
        } catch (NoResultException | org.hibernate.exception.LockAcquisitionException ex) {
            LOG.trace("Message:[{}] no result for message id", messageId);
            return null;
        }
    }

    public void changeStatus(final MessagingLock messagingLock, final String messageState) {
        final MessagingLock messagingLockForMessageId = findMessagingLockForMessageId(messagingLock.getMessageId());
        messagingLockForMessageId.setNextAttempt(messagingLock.getNextAttempt());
        messagingLockForMessageId.setSendAttempts(messagingLock.getSendAttempts());
        messagingLockForMessageId.setMessageState(messagingLock.getMessageState());
        entityManager.persist(messagingLockForMessageId);
        /*final Map<String, String> params = new HashMap<>();
        params.put(MESSAGE_ID, messagingLock.getMessageId());
        final String selectForUpdate = unlockByMessageIdQuery.get(domibusConfigurationService.getDataBaseEngine());
        SqlRowSet sqlRowSet = null;
        sqlRowSet = jdbcTemplate.queryForRowSet(selectForUpdate, params);
        sqlRowSet.next();
        final Map<String, Object> updateParams = new HashMap<>();
        final long id = sqlRowSet.getLong(ID_PK_COLUMN);
        updateParams.put(ID_PK_PARAM, id);
        updateParams.put("SEND_ATTEMPT", messagingLock.getSendAttempts());
        updateParams.put("NEXT_ATTEMPT", messagingLock.getNextAttempt());
        updateParams.put("MESSAGE_STATE", messageState);
        jdbcTemplate.update("UPDATE TB_MESSAGING_LOCK SET MESSAGE_STATE=:MESSAGE_STATE,SEND_ATTEMPTS=:SEND_ATTEMPT,NEXT_ATTEMPT=:NEXT_ATTEMPT WHERE ID_PK=:idPk", updateParams);*/
    }


    @Override
    public void save(final MessagingLock messagingLock) {
        entityManager.persist(messagingLock);
    }

    @Override
    public void delete(final String messageId) {
        Query query = entityManager.createNamedQuery("MessagingLock.delete");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();
    }


    public void processing(final String messageId) {
        Query query = entityManager.createNamedQuery("MessagingLock.delete");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();
    }

    @Override
    public void delete(final MessagingLock messagingLock) {
        entityManager.remove(messagingLock);
    }

    @Override
    public MessagingLock findMessagingLockForMessageId(final String messageId) {
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findForMessageId", MessagingLock.class);
        namedQuery.setParameter("MESSAGE_ID", messageId);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException nr) {
            return null;
        }
    }

    @Override
    public List<MessagingLock> findStaledMessages() {
        TypedQuery<MessagingLock> query = entityManager.createNamedQuery("MessagingLock.findStalledMessages", MessagingLock.class);
        return query.getResultList();
    }

    @Override
    public List<MessagingLock> findDeleteMessages() {
        TypedQuery<MessagingLock> query = entityManager.createNamedQuery("MessagingLock.findDeletedMessage", MessagingLock.class);
        return query.getResultList();
    }


    @Override
    public List<MessagingLock> findReadyToPull(final String mpc, final String initiator) {
        final TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findReadyToPull", MessagingLock.class);
        namedQuery.setFirstResult(0);
        namedQuery.setMaxResults(100);
        namedQuery.setParameter("MPC", mpc);
        namedQuery.setParameter("INITIATOR", initiator);
        return namedQuery.getResultList();
    }

    @Override
    public List<MessagingLock> findWaitingForReceipt() {
        final TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findWaitingForReceipt", MessagingLock.class);
        return namedQuery.getResultList();
    }
}
