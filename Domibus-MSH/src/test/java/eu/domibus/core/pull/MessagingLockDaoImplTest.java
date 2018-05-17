package eu.domibus.core.pull;

import eu.domibus.api.configuration.DataBaseEngine;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.ebms3.common.model.MessagingLock;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class MessagingLockDaoImplTest {


    @Injectable
    private EntityManager entityManager;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Tested
    private MessagingLockDaoImpl messagingLockDao;

    @Before
    public void init() {

    }

    @Test
    public void getNextPullMessageToProcessFirstAttempt(@Mocked final SqlRowSet sqlRowSet) {
        final long idPk = 6;
        final String messageId = "firstTimeMessageId";

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.MYSQL;

            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE", withAny(params));
            result = sqlRowSet;

            sqlRowSet.next();
            result = true;

            sqlRowSet.getString(1);
            result = messageId;

            sqlRowSet.getDate(2);
            result = new java.sql.Date(System.currentTimeMillis() + 20000);

            sqlRowSet.getInt(3);
            result = 0;

            sqlRowSet.getInt(4);
            result = 5;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(MessageStaledState.FIRST_ATTEMPT, nextPullMessageToProcess.getState());
        assertEquals(null, nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE", params = withCapture());
            assertEquals(idPk, params.get(MessagingLockDaoImpl.ID_PK));

            jdbcTemplate.update("DELETE FROM TB_MESSAGING_LOCK WHERE ID_PK=:idPk", params = withCapture());
            assertEquals(idPk, params.get(MessagingLockDaoImpl.ID_PK));
        }};
    }

    @Test
    public void getNextPullMessageToProcessFurtherAttempt(@Mocked final SqlRowSet sqlRowSet) {
        final long idPk = 6;
        final String messageId = "furtherAttemptMessageId";

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.MYSQL;

            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE", withAny(params));
            result = sqlRowSet;

            sqlRowSet.next();
            result = true;

            sqlRowSet.getString(1);
            result = messageId;

            sqlRowSet.getDate(2);
            result = new java.sql.Date(System.currentTimeMillis() + 20000);

            sqlRowSet.getInt(3);
            result = 1;

            sqlRowSet.getInt(4);
            result = 5;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(MessageStaledState.FURTHER_ATTEMPT, nextPullMessageToProcess.getState());
        assertEquals(null, nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE", params = withCapture());
            assertEquals(idPk, params.get(MessagingLockDaoImpl.ID_PK));

            jdbcTemplate.update("DELETE FROM TB_MESSAGING_LOCK WHERE ID_PK=:idPk", params = withCapture());
            assertEquals(idPk, params.get(MessagingLockDaoImpl.ID_PK));
        }};
    }

    @Test
    public void getNextPullMessageToProcessStaled(@Mocked final SqlRowSet sqlRowSet) {
        final long idPk = 6;
        final String messageId = "staledMessageId";
        final java.sql.Date staledDate = new java.sql.Date(System.currentTimeMillis() - 20000);

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.MYSQL;

            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE", withAny(params));
            result = sqlRowSet;

            sqlRowSet.next();
            result = true;

            sqlRowSet.getString(1);
            result = messageId;

            sqlRowSet.getDate(2);
            result = staledDate;

            sqlRowSet.getInt(3);
            result = 1;

            sqlRowSet.getInt(4);
            result = 5;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(MessageStaledState.STALED, nextPullMessageToProcess.getState());
        assertEquals(String.format("Maximum time to send the message has been reached:[%tc]", staledDate), nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE", params = withCapture());
            assertEquals(idPk, params.get(MessagingLockDaoImpl.ID_PK));

            jdbcTemplate.update("DELETE FROM TB_MESSAGING_LOCK WHERE ID_PK=:idPk", params = withCapture());
            assertEquals(idPk, params.get(MessagingLockDaoImpl.ID_PK));
        }};
    }

    @Test
    public void getNextPullMessageToProcessMaxAttemptsReached(@Mocked final SqlRowSet sqlRowSet) {
        final long idPk = 6;
        final String messageId = "maxAttemptMessageId";
        final java.sql.Date staledDate = new java.sql.Date(System.currentTimeMillis() + 20000);

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.H2;

            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE", withAny(params));
            result = sqlRowSet;

            sqlRowSet.next();
            result = true;

            sqlRowSet.getString(1);
            result = messageId;

            sqlRowSet.getDate(2);
            result = staledDate;

            sqlRowSet.getInt(3);
            result = 5;

            sqlRowSet.getInt(4);
            result = 5;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(MessageStaledState.STALED, nextPullMessageToProcess.getState());
        assertEquals(String.format("Maximum number of attempts to send the message has been reached:[%d]", 5), nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE", params = withCapture());
            assertEquals(idPk, params.get(MessagingLockDaoImpl.ID_PK));

            jdbcTemplate.update("DELETE FROM TB_MESSAGING_LOCK WHERE ID_PK=:idPk", params = withCapture());
            assertEquals(idPk, params.get(MessagingLockDaoImpl.ID_PK));
        }};
    }

    @Test
    public void getNextPullMessageToProcessNoMessage(@Mocked final SqlRowSet sqlRowSet) {
        final long idPk = 6;
        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.ORACLE;

            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE NOWAIT", withAny(params));
            result = sqlRowSet;

            sqlRowSet.next();
            result = false;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNull(nextPullMessageToProcess);


        new Verifications() {{
            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE NOWAIT", params = withCapture());
            times = 1;
            assertEquals(idPk, params.get(MessagingLockDaoImpl.ID_PK));

            jdbcTemplate.update("DELETE FROM TB_MESSAGING_LOCK WHERE ID_PK=:idPk", withAny(params));
            times = 0;
        }};
    }

    @Test
    public void getNextPullMessageToProcessMessageLocked(@Mocked final SqlRowSet sqlRowSet) {
        final long idPk = 6;
        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.ORACLE;

            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE NOWAIT", withAny(params));
            result = new CannotAcquireLockException("");

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNull(nextPullMessageToProcess);


        new Verifications(){{
            Map params = new HashMap();
            jdbcTemplate.queryForRowSet("SELECT ml.MESSAGE_ID,ml.MESSAGE_STALED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX  FROM TB_MESSAGING_LOCK ml where ml.ID_PK=:idPk FOR UPDATE NOWAIT", params = withCapture());
            times = 1;
            assertEquals(idPk, params.get(MessagingLockDaoImpl.ID_PK));

            jdbcTemplate.update("DELETE FROM TB_MESSAGING_LOCK WHERE ID_PK=:idPk", withAny(params));
            times = 0;
        }};
    }


    @Test
    public void save() {
        final MessagingLock messagingLock=new MessagingLock();
        messagingLockDao.save(messagingLock);
        new Verifications(){{
           entityManager.persist(messagingLock);
        }};
    }

    @Test
    public void delete(@Mocked final Query query) {
        final String messageId = "messageId";
        new Expectations(){{
           entityManager.createNamedQuery("MessagingLock.delete");
           result=query;
        }};
        messagingLockDao.delete(messageId);
        new Verifications(){{
            query.setParameter("MESSAGE_ID",messageId);
            query.executeUpdate();
        }};

    }
}