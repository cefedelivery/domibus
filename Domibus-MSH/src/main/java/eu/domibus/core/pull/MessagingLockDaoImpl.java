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
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public static final String IDPK = "idpk";

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    private final static Map<DataBaseEngine, String> lockByIdQuery = new HashMap<>();

    private final static Map<DataBaseEngine, String> lockByMessageIdQuery = new HashMap<>();

    private final static Map<DataBaseEngine, String> unlockByMessageIdQuery = new HashMap<>();

    private final static Map<DataBaseEngine, String> retrieveMessageReady = new HashMap<>();

    static {

      /*  retrieveMessageReady.put(DataBaseEngine.ORACLE, "SELECT * FROM (SELECT ID_PK FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' ORDER BY ID_PK) where ROWNUM <= 50");
        retrieveMessageReady.put(DataBaseEngine.MYSQL, "SELECT ID_PK FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' ORDER BY ID_PK LIMIT 50");
        retrieveMessageReady.put(DataBaseEngine.H2, "SELECT ID_PK FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' ORDER BY ID_PK");*/

        lockByIdQuery.put(DataBaseEngine.MYSQL, "SELECT MESSAGE_ID,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX, MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=:idpk FOR UPDATE");
        lockByIdQuery.put(DataBaseEngine.ORACLE, "SELECT MESSAGE_ID,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX, MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' AND ml.ID_PK=:idpk FOR UPDATE NOWAIT");
        lockByIdQuery.put(DataBaseEngine.H2, "SELECT MESSAGE_ID,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX, MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=:idpk FOR UPDATE");

        lockByMessageIdQuery.put(DataBaseEngine.MYSQL, "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_ID=?1 FOR UPDATE");
        lockByMessageIdQuery.put(DataBaseEngine.ORACLE, "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_ID=?1 FOR UPDATE NOWAIT");
        lockByMessageIdQuery.put(DataBaseEngine.H2, "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_ID=?1 FOR UPDATE");

       /* unlockByMessageIdQuery.put(DataBaseEngine.MYSQL, "SELECT ml.ID_PK,ml.MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='PROCESS' and ml.MESSAGE_ID=:MESSAGE_ID FOR UPDATE");
        unlockByMessageIdQuery.put(DataBaseEngine.H2, "SELECT ml.ID_PK,ml.MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='PROCESS' and ml.MESSAGE_ID=:MESSAGE_ID FOR UPDATE");
        unlockByMessageIdQuery.put(DataBaseEngine.ORACLE, "SELECT ml.ID_PK,ml.MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='PROCESS' and ml.MESSAGE_ID=:MESSAGE_ID FOR UPDATE  NOWAIT");*/

    }

   /* @PostConstruct
    public void init(){
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    updateOperation1();
                    updateOperation2();
                } catch (SomeBusinessExeption ex) {
                    status.setRollbackOnly();
                }
            }
        });
    }*/

    @Autowired
    @Qualifier("domibusJDBC-XADataSource")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PullMessageId getNextPullMessageToProcess(final Integer idPk) {
        Map<String, Object> params = new HashMap<>();
        /*final String retrieveMessageReadyQuery = retrieveMessageReady.get(domibusConfigurationService.getDataBaseEngine());
        final SqlRowSet readyRowSet = jdbcTemplate.queryForRowSet(retrieveMessageReadyQuery, params);
        while (readyRowSet.next()) {*/
        try {
            params.put(IDPK, idPk);
            final String sql = lockByIdQuery.get(domibusConfigurationService.getDataBaseEngine());
            final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, params);
            final boolean next = sqlRowSet.next();
            if (!next) {
                return null;
            }

            final String messageId = sqlRowSet.getString(1);
            final int sendAttempts = sqlRowSet.getInt(2);
            final int sendAttemptsMax = sqlRowSet.getInt(3);
            final Timestamp messageStaled = getTimestamp(sqlRowSet.getObject(4));

            final String updateSql = "UPDATE TB_MESSAGING_LOCK ml SET ml.MESSAGE_STATE=:MESSAGE_STATE WHERE ml.ID_PK=:idpk";
            final Timestamp currentDate = new Timestamp(System.currentTimeMillis());
            LOG.debug("expiration date[{}], current date[{}] ", messageStaled, currentDate);
            if (messageStaled.compareTo(currentDate) < 0) {
                params.put("MESSAGE_STATE", MessageState.DEL.name());
                jdbcTemplate.update(updateSql, params);
                return new PullMessageId(messageId, EXPIRED, String.format("Maximum time to send the message has been reached:[%tc]", messageStaled));
            }
            LOG.debug("sendattempts[{}], sendattemptsmax[{}]", sendAttempts, sendAttemptsMax);
            if (sendAttempts >= sendAttemptsMax) {
                params.put("MESSAGE_STATE", MessageState.DEL.name());
                jdbcTemplate.update(updateSql, params);
                return new PullMessageId(messageId, EXPIRED, String.format("Maximum number of attempts to send the message has been reached:[%d]", sendAttempts));
            }
            if (sendAttempts >= 0) {
                params.put("MESSAGE_STATE", MessageState.PROCESS.name());
                jdbcTemplate.update(updateSql, params);
            }
            if (sendAttempts > 0) {
                return new PullMessageId(messageId, RETRY);
            }
            return new PullMessageId(messageId);
        } catch (Exception e) {
            //LOG.trace("MessageLock[{}] lock could not be acquired", idPk, e);
            LOG.debug("MessageLock[{}] lock could not be acquired", idPk);
            return null;
        }


    }






   /* @Override
    public void readyToDelete(final String messageId) {
        final MessagingLock messagingLockForMessageId = findMessagingLockForMessageId(messageId);
        messagingLockForMessageId.setMessageState(MessageState.DEL);
        entityManager.persist(messagingLockForMessageId);
    }*/

   /* @Override
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional
    public Integer getLock(final String messageId) {
        LOG.trace("Message[{}] get lock",messageId);
        try {
            Map<String,Object> params=new HashMap<>();
            params.put(MESSAGE_ID,messageId);
            final String lockByMesageIdQuery = lockByMessageIdQuery.get(domibusConfigurationService.getDataBaseEngine());
            final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(lockByMesageIdQuery, params);
            final String updateSql = "UPDATE TB_MESSAGING_LOCK ml SET ml.MESSAGE_STATE=:MESSAGE_STATE WHERE ml.ID_PK=:idpk";
            while (sqlRowSet.next()){
                final Integer id= sqlRowSet.getInt(1);
                params.put(IDPK,id);
                params.put("MESSAGE_STATE",MessageState.PROCESS.name());
                jdbcTemplate.update(updateSql, params);
                return id;
            }
        }catch (Exception ex){
            LOG.trace("Message[{}]:unable to lock", messageId);
        }
        return null;
    }*/


    public MessagingLock getLock(final String messageId) {

        try {
            LOG.debug("Message[{}] Getting lock", messageId);
            Query q = entityManager.createNativeQuery(lockByMessageIdQuery.get(domibusConfigurationService.getDataBaseEngine()), MessagingLock.class);
            q.setParameter(1, messageId);
            MessagingLock messagingLock = (MessagingLock) q.getSingleResult();
            return messagingLock;
        } catch (Exception ex) {
            LOG.error("Message:[{}] no result for message id", messageId);
            return null;
        }
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
    public List<MessagingLock> findDeletedMessages() {
        TypedQuery<MessagingLock> query = entityManager.createNamedQuery("MessagingLock.findDeletedMessages", MessagingLock.class);
        return query.getResultList();
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<MessagingLock> findReadyToPull(final String mpc, final String initiator) {
        final TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findReadyToPull", MessagingLock.class);
        namedQuery.setFirstResult(0);
        namedQuery.setMaxResults(3);
        namedQuery.setParameter("MPC", mpc);
        namedQuery.setParameter("INITIATOR", initiator);
        return namedQuery.getResultList();
    }

    @Override
    public List<MessagingLock> findWaitingForReceipt() {
        final TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findWaitingForReceipt", MessagingLock.class);
        return namedQuery.getResultList();
    }

    @Override
    public MessagingLock getMessagingLock(Integer id) {
        return entityManager.find(MessagingLock.class, id);
    }

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

}
