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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import static eu.domibus.core.pull.MessageStaledState.FURTHER_ATTEMPT;
import static eu.domibus.core.pull.MessageStaledState.STALED;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
@Repository
public class MessagingLockDaoImpl implements MessagingLockDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockDaoImpl.class);

    public static final String MPC = "MPC";

    public static final String MESSAGE_ID = "MESSAGE_ID";

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    private final static Map<DataBaseEngine, String> databaseSpecificQueries = new HashMap<>();

    static {
        databaseSpecificQueries.put(DataBaseEngine.MYSQL, "SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE");
        databaseSpecificQueries.put(DataBaseEngine.H2, "SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE");
        databaseSpecificQueries.put(DataBaseEngine.ORACLE, "SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE NOWAIT");
    }


    @Autowired
    @Qualifier("domibusJDBC-XADataSource")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PullMessageId getNextPullMessageToProcess(final Long idPk) {
        final Map<String, Long> params = new HashMap<>();
        params.put("idPk", idPk);
        try {
            final String databaseSpecificQuery = databaseSpecificQueries.get(domibusConfigurationService.getDataBaseEngine());
            final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(databaseSpecificQuery, params);
            while (sqlRowSet.next()) {
                final String messageId = sqlRowSet.getString(1);
                final Date messageStaled = sqlRowSet.getDate(2);
                final int sendAttempts = sqlRowSet.getInt(3);
                final int sendAttemptsMax = sqlRowSet.getInt(4);
                jdbcTemplate.update("DELETE FROM TB_MESSAGING_LOCK WHERE ID_PK=:idPk", params);
                if (messageStaled.compareTo(new Date(System.currentTimeMillis())) < 0) {
                    return new PullMessageId(messageId, STALED, String.format("Maximum time to send the message has been reached:[%tc]", messageStaled));
                }
                if (sendAttempts >= sendAttemptsMax) {
                    return new PullMessageId(messageId, STALED, String.format("Maximum number of attempts to send the message has been reached:[%d]", 35));
                }
                if (sendAttempts > 0) {
                    return new PullMessageId(messageId, FURTHER_ATTEMPT, "");
                }
                return new PullMessageId(messageId);
            }
        } catch (CannotAcquireLockException ex) {

        }
        return null;
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

}
