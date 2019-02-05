package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.core.pull.PullMessageState.EXPIRED;
import static eu.domibus.core.pull.PullMessageState.RETRY;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
@Repository
public class MessagingLockDaoImpl implements MessagingLockDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockDaoImpl.class);

    private static final String MESSAGE_ID = "MESSAGE_ID";

    protected static final String IDPK = "idpk";

    private static final String MPC = "MPC";

    private static final String INITIATOR = "INITIATOR";

    protected static final String MESSAGE_STATE = "MESSAGE_STATE";

    protected static final String LOCK_BY_ID_QUERY = "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=?1 FOR UPDATE";

    protected static final String LOCK_BY_MESSAGE_ID_QUERY = "SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_ID=?1 FOR UPDATE";

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    private JdbcTemplate jdbcTemplate;

    private JdbcTemplate lockJdbcTemplate;

    @Autowired
    @Qualifier("domibusJDBC-XADataSource")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.lockJdbcTemplate = new JdbcTemplate(dataSource);
        this.lockJdbcTemplate.setMaxRows(1);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @eu.domibus.common.statistics.Timer(clazz = MessagingLockDaoImpl.class, timerName = "get_next_message_to_process")
    public PullMessageId getOracleNextPullMessageToProcess(final String initiator, final String mpc) {

        final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select ml.ID_PK from TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE = 'READY' and ml.MPC=? and ml.INITIATOR=? and ml.NEXT_ATTEMPT<CURRENT_TIMESTAMP and ml.MESSAGE_STALED>CURRENT_TIMESTAMP order by ml.ID_PK", mpc, initiator);
        while (sqlRowSet.next()) {
            final Long idPk = sqlRowSet.getLong(1);
            final PullMessageId messageId = getMessageId(idPk);
            if (messageId != null) {
                return messageId;
            }
        }
        return null;
    }

    @Override
    @eu.domibus.common.statistics.Timer(clazz = MessagingLockDaoImpl.class, timerName = "get_pull_message_id_on_pkid")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PullMessageId getMessageIdInTransaction(final Long idPk) {
        return getMessageId(idPk);
        /*return template.execute(paramTransactionStatus -> {
            LOG.info("Transaction new:[{}] "+paramTransactionStatus.isNewTransaction());
            return getMessageId(idPk);
        });*/

    }

    private PullMessageId getMessageId(final Long idPk) {
        ResultSetExtractor<PullMessageId> rch = (resultSet) -> {
            final boolean next = resultSet.next();
            if (!next) {
                return null;
            }
            String messageId = null;

            messageId = resultSet.getString("MESSAGE_ID");
            final int sendAttempts = resultSet.getInt("SEND_ATTEMPTS");
            final int sendAttemptsMax = resultSet.getInt("SEND_ATTEMPTS_MAX");
            final Date messageStaled = resultSet.getDate("MESSAGE_STALED");
            final Timestamp currentDate = new Timestamp(System.currentTimeMillis());
            LOG.debug("expiration date[{}], current date[{}] ", messageStaled, currentDate);
            if (messageStaled.compareTo(currentDate) < 0) {
                LOG.trace("Updating to delete");
                try {
                    resultSet.updateString("MESSAGE_STATE", "DEL");
                    resultSet.updateRow();
                } catch (SQLException se) {
                    //I think I am facing this weird exception :
                    //https://stackoverflow.com/questions/30531854/invalid-operation-for-read-only-resultset-when-using-select-for-update-nowait-in
                    LOG.trace("", se);
                    return null;
                }

                return new PullMessageId(messageId, EXPIRED, String.format("Maximum time to send the message has been reached:[%tc]", messageStaled));
            }
            LOG.debug("sendattempts[{}], sendattemptsmax[{}]", sendAttempts, sendAttemptsMax);
            if (sendAttempts >= sendAttemptsMax) {
                LOG.trace("Updating to delete");
                try {
                    resultSet.updateString("MESSAGE_STATE", "DEL");
                    resultSet.updateRow();
                } catch (SQLException se) {
                    //I think I am facing this weird exception :
                    //https://stackoverflow.com/questions/30531854/invalid-operation-for-read-only-resultset-when-using-select-for-update-nowait-in
                    LOG.trace("", se);
                    return null;
                }
                return new PullMessageId(messageId, EXPIRED, String.format("Maximum number of attempts to send the message has been reached:[%d]", sendAttempts));
            }
            try {
                resultSet.updateString("MESSAGE_STATE", "PROCESS");
                resultSet.updateRow();
            } catch (SQLException se) {
                //I think I am facing this weird exception :
                //https://stackoverflow.com/questions/30531854/invalid-operation-for-read-only-resultset-when-using-select-for-update-nowait-in
                LOG.trace("", se);
                return null;
            }

            if (sendAttempts > 0) {
                return new PullMessageId(messageId, RETRY);
            }

            return new PullMessageId(messageId);
        };

        try {
            PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(
                    "select ml.ID_PK,ml.MESSAGE_ID,ml.SEND_ATTEMPTS,ml.SEND_ATTEMPTS_MAX,ml.MESSAGE_STALED,ml.MESSAGE_STATE  from TB_MESSAGING_LOCK ml where ml.ID_PK =? and ml.MESSAGE_STATE = 'READY' for update nowait",
                    new int[]{Types.BIGINT});
            pscf.setUpdatableResults(true);
            pscf.setResultSetType(ResultSet.TYPE_FORWARD_ONLY);
            return lockJdbcTemplate.query(pscf.newPreparedStatementCreator(new Object[]{idPk}), rch);
        } catch (CannotAcquireLockException e) {
            LOG.debug("Message with idPk:[{}] locked, trying to pick next one", idPk, e);
        }
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @eu.domibus.common.statistics.Timer(clazz = MessagingLockDaoImpl.class, timerName = "get_next_message_to_process")
    public PullMessageId getNextPullMessageToProcess(final Integer idPk) {
        try {
            Query q = entityManager.createNativeQuery(LOCK_BY_ID_QUERY, MessagingLock.class);
            q.setParameter(1, idPk);
            final MessagingLock messagingLock = (MessagingLock) q.getSingleResult();
            LOG.debug("[getNextPullMessageToProcess]:id[{}] locked", idPk);
            final String messageId = messagingLock.getMessageId();
            final int sendAttempts = messagingLock.getSendAttempts();
            final int sendAttemptsMax = messagingLock.getSendAttemptsMax();
            final Date messageStaled = messagingLock.getStaled();

            final Timestamp currentDate = new Timestamp(System.currentTimeMillis());
            LOG.debug("expiration date[{}], current date[{}] ", messageStaled, currentDate);
            if (messageStaled.compareTo(currentDate) < 0) {
                messagingLock.setMessageState(MessageState.DEL);
                return new PullMessageId(messageId, EXPIRED, String.format("Maximum time to send the message has been reached:[%tc]", messageStaled));
            }
            LOG.debug("sendattempts[{}], sendattemptsmax[{}]", sendAttempts, sendAttemptsMax);
            if (sendAttempts >= sendAttemptsMax) {
                messagingLock.setMessageState(MessageState.DEL);
                return new PullMessageId(messageId, EXPIRED, String.format("Maximum number of attempts to send the message has been reached:[%d]", sendAttempts));
            }
            if (sendAttempts >= 0) {
                messagingLock.setMessageState(MessageState.PROCESS);
            }
            if (sendAttempts > 0) {
                return new PullMessageId(messageId, RETRY);
            }
            return new PullMessageId(messageId);
        } catch (NoResultException ne) {
            LOG.trace("No more message ready message for id:[{}], has been process by another pull request", idPk, ne);
            return null;
        } catch (Exception e) {
            LOG.error("MessageLock[{}] lock could not be acquired", idPk, e);
            return null;
        }


    }


    public MessagingLock getLock(final String messageId) {
        try {
            LOG.debug("Message[{}] Getting lock", messageId);
            Query q = entityManager.createNativeQuery(LOCK_BY_MESSAGE_ID_QUERY, MessagingLock.class);
            q.setParameter(1, messageId);
            return (MessagingLock) q.getSingleResult();
        } catch (NoResultException nr) {
            LOG.trace("Message:[{}] lock not found. It is has been removed by another process.", messageId, nr);
            return null;
        } catch (Exception ex) {
            LOG.warn("Message:[{}] lock could not be acquire. It is probably handled by another process.", messageId, ex);
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
        namedQuery.setParameter(MESSAGE_ID, messageId);
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
    @eu.domibus.common.statistics.Timer(clazz = MessagingLockDaoImpl.class, timerName = "find_ready_to_pull")
    public List<MessagingLock> findReadyToPull(final String mpc, final String initiator) {
        final TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findReadyToPull", MessagingLock.class);
        namedQuery.setFirstResult(0);
        namedQuery.setMaxResults(50);
        namedQuery.setParameter(MPC, mpc);
        namedQuery.setParameter(INITIATOR, initiator);
        return namedQuery.getResultList();
    }

    @Override
    public List<MessagingLock> findWaitingForReceipt() {
        final TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findWaitingForReceipt", MessagingLock.class);
        return namedQuery.getResultList();
    }


}
