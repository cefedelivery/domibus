package eu.domibus.core.pull;

import eu.domibus.api.configuration.DataBaseEngine;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
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

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    private final static Map<DataBaseEngine, String> lockByIdQuery = new HashMap<>();

    private final static Map<DataBaseEngine, String> lockByMessageIdQuery = new HashMap<>();

    private final static Map<DataBaseEngine, String> unlockByMessageIdQuery = new HashMap<>();

    static {
        lockByIdQuery.put(DataBaseEngine.MYSQL, "SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=:idPk FOR UPDATE");
        lockByIdQuery.put(DataBaseEngine.H2, "SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=:idPk FOR UPDATE");
        lockByIdQuery.put(DataBaseEngine.ORACLE, "SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=:idPk FOR UPDATE NOWAIT");

        lockByMessageIdQuery.put(DataBaseEngine.MYSQL, "SELECT ml.ID_PK,ml.MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.MESSAGE_ID=:MESSAGE_ID FOR UPDATE");
        lockByMessageIdQuery.put(DataBaseEngine.H2, "SELECT ml.ID_PK,ml.MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.MESSAGE_ID=:MESSAGE_ID FOR UPDATE");
        lockByMessageIdQuery.put(DataBaseEngine.ORACLE, "SELECT ml.ID_PK,ml.MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.MESSAGE_ID=:MESSAGE_ID FOR UPDATE NOWAIT");

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PullMessageId getNextPullMessageToProcess(final Long idPk) {
        final Map<String, Object> params = new HashMap<>();
        params.put(ID_PK_PARAM, idPk);
        try {
            final String databaseSpecificQuery = lockByIdQuery.get(domibusConfigurationService.getDataBaseEngine());
            final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(databaseSpecificQuery, params);
            while (sqlRowSet.next()) {
                final String messageId = sqlRowSet.getString(1);
                final Timestamp messageStaled = getTimestamp(sqlRowSet.getObject(2));
                final int sendAttempts = sqlRowSet.getInt(3);
                final int sendAttemptsMax = sqlRowSet.getInt(4);
                params.put("MESSAGE_STATE", PROCESS);
                LOG.debug("[getNextPullMessageToProcess]:Message:[{}] delete lock ", messageId);
                jdbcTemplate.update("UPDATE TB_MESSAGING_LOCK SET MESSAGE_STATE=:MESSAGE_STATE WHERE ID_PK=:idPk", params);
                if (messageStaled.compareTo(new Date(System.currentTimeMillis())) < 0) {
                    return new PullMessageId(messageId, EXPIRED, String.format("Maximum time to send the message has been reached:[%tc]", messageStaled));
                }
                if (sendAttempts >= sendAttemptsMax) {
                    return new PullMessageId(messageId, EXPIRED, String.format("Maximum number of attempts to send the message has been reached:[%d]", sendAttempts));
                }
                if (sendAttempts > 0) {
                    return new PullMessageId(messageId, RETRY);
                }
                return new PullMessageId(messageId);
            }
        } catch (CannotAcquireLockException ex) {
            LOG.trace("MessagingLock:[{}] could not be locked.", ex, idPk);
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
        changeStatus(messagingLock, READY);

    }

    @Override
    public void deleteLock(final String messageId) {
        final Map<String, String> params = new HashMap<>();
        params.put(MESSAGE_ID, messageId);
        final String selectForUpdate = unlockByMessageIdQuery.get(domibusConfigurationService.getDataBaseEngine());
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(selectForUpdate, params);
        sqlRowSet.next();
        final Map<String, Object> updateParams = new HashMap<>();
        final long id = sqlRowSet.getLong(ID_PK_COLUMN);
        updateParams.put(ID_PK_PARAM, id);
        updateParams.put("MESSAGE_STATE", DEL);
        jdbcTemplate.update("UPDATE TB_MESSAGING_LOCK SET MESSAGE_STATE=:MESSAGE_STATE WHERE ID_PK=:idPk", updateParams);

    }

    public void changeStatus(final MessagingLock messagingLock, final String messageState) {
        final Map<String, String> params = new HashMap<>();
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
        jdbcTemplate.update("UPDATE TB_MESSAGING_LOCK SET MESSAGE_STATE=:MESSAGE_STATE,SEND_ATTEMPTS=:SEND_ATTEMPT,NEXT_ATTEMPT=:NEXT_ATTEMPT WHERE ID_PK=:idPk", updateParams);
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PullLockAckquire lockAndDeleteMessageLock(final String messageId) {
        final Map<String, String> params = new HashMap<>();
        params.put(MESSAGE_ID, messageId);

        final String selectForDelete = lockByMessageIdQuery.get(domibusConfigurationService.getDataBaseEngine());
        SqlRowSet sqlRowSet = null;
        try {
            sqlRowSet = jdbcTemplate.queryForRowSet(selectForDelete, params);
        } catch (CannotAcquireLockException | org.springframework.dao.DeadlockLoserDataAccessException ex) {
            LOG.trace("MessagingLock:[{}] could not be locked.", messageId, ex);
            return null;
        }
        if (sqlRowSet.next()) {
            final Map<String, Long> idpkd = new HashMap<>();
            final long id = sqlRowSet.getLong(ID_PK_COLUMN);
            final Timestamp messageStaled = getTimestamp(sqlRowSet.getObject(MESSAGE_STALED_COLUM));
            idpkd.put(ID_PK_PARAM, id);
            jdbcTemplate.update("UPDATE TB_MESSAGING_LOCK SET MESSAGE_STATE='PROCESS' WHERE ID_PK=:idPk", idpkd);
            return new PullLockAckquire(id, messageStaled.getTime());
        }
        return null;
    }
}
