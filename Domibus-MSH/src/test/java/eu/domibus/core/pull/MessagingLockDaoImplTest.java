package eu.domibus.core.pull;

import eu.domibus.api.configuration.DataBaseEngine;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.ebms3.common.model.MessageState;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
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
    public void getNextPullMessageToProcessFirstAttempt(
            @Mocked final SqlRowSet sqlRowSet) {
        final int idPk = 6;

        final String messageId = "furtherAttemptMessageId";

        final int sendAttempts = 0;

        final int sendAttemptsMax = 5;

        final Timestamp timestamp=new Timestamp(System.currentTimeMillis()+20000);

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.MYSQL;

            final Map arg = new HashMap<>();
            jdbcTemplate.queryForRowSet("SELECT MESSAGE_ID,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX, MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=:idpk FOR UPDATE", withAny(arg));
            result = sqlRowSet;

            sqlRowSet.next();
            result = true;

            sqlRowSet.getString(1);
            result = messageId;

            sqlRowSet.getInt(2);
            result = sendAttempts;

            sqlRowSet.getInt(3);
            result = sendAttemptsMax;

            sqlRowSet.getObject(4);
            result = timestamp;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.FIRST_ATTEMPT, nextPullMessageToProcess.getState());
        assertEquals(null, nextPullMessageToProcess.getStaledReason());

        new Verifications() {{
            Map<String, Object> params;
            jdbcTemplate.update("UPDATE TB_MESSAGING_LOCK ml SET ml.MESSAGE_STATE=:MESSAGE_STATE WHERE ml.ID_PK=:idpk", params = withCapture());
            assertEquals(6, params.get(MessagingLockDaoImpl.IDPK));
            assertEquals(MessageState.PROCESS.name(), params.get(MessagingLockDaoImpl.MESSAGE_STATE));

        }};
    }

    @Test
    public void getNextPullMessageToProcessWithRetry(
            @Mocked final SqlRowSet sqlRowSet) {
        final int idPk = 6;

        final String messageId = "furtherAttemptMessageId";

        final int sendAttempts = 1;

        final int sendAttemptsMax = 5;

        final Timestamp timestamp=new Timestamp(System.currentTimeMillis()+10000);

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.MYSQL;

            final Map arg = new HashMap<>();
            jdbcTemplate.queryForRowSet("SELECT MESSAGE_ID,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX, MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=:idpk FOR UPDATE", withAny(arg));
            result = sqlRowSet;

            sqlRowSet.next();
            result = true;

            sqlRowSet.getString(1);
            result = messageId;

            sqlRowSet.getInt(2);
            result = sendAttempts;

            sqlRowSet.getInt(3);
            result = sendAttemptsMax;

            sqlRowSet.getObject(4);
            result = timestamp;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess((int) idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.RETRY, nextPullMessageToProcess.getState());
        assertEquals(null, nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            Map<String, Object> params;
            jdbcTemplate.update("UPDATE TB_MESSAGING_LOCK ml SET ml.MESSAGE_STATE=:MESSAGE_STATE WHERE ml.ID_PK=:idpk", params = withCapture());
            assertEquals(6, params.get(MessagingLockDaoImpl.IDPK));
            assertEquals(MessageState.PROCESS.name(), params.get(MessagingLockDaoImpl.MESSAGE_STATE));

        }};
    }

    @Test
    public void getNextPullMessageToProcessExpired(
            @Mocked final SqlRowSet sqlRowSet) {

        final int idPk = 6;

        final String messageId = "furtherAttemptMessageId";

        final int sendAttempts = 1;

        final int sendAttemptsMax = 5;

        final Timestamp timestamp=new Timestamp(System.currentTimeMillis()-20000);

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.MYSQL;

            final Map arg = new HashMap<>();
            jdbcTemplate.queryForRowSet("SELECT MESSAGE_ID,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX, MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=:idpk FOR UPDATE", withAny(arg));
            result = sqlRowSet;

            sqlRowSet.next();
            result = true;

            sqlRowSet.getString(1);
            result = messageId;

            sqlRowSet.getInt(2);
            result = sendAttempts;

            sqlRowSet.getInt(3);
            result = sendAttemptsMax;

            sqlRowSet.getObject(4);
            result = timestamp;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess((int) idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.EXPIRED, nextPullMessageToProcess.getState());
        assertNotNull(null, nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            Map<String, Object> params;
            jdbcTemplate.update("UPDATE TB_MESSAGING_LOCK ml SET ml.MESSAGE_STATE=:MESSAGE_STATE WHERE ml.ID_PK=:idpk", params = withCapture());
            assertEquals(6, params.get(MessagingLockDaoImpl.IDPK));
            assertEquals(MessageState.DEL.name(), params.get(MessagingLockDaoImpl.MESSAGE_STATE));

        }};
    }

    @Test
    public void getNextPullMessageToProcessMaxAttemptsReached(
            @Mocked final SqlRowSet sqlRowSet,
            @Mocked final Timestamp timestamp) {
        final int idPk = 6;

        final String messageId = "furtherAttemptMessageId";

        final int sendAttempts = 5;

        final int sendAttemptsMax = 5;

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.MYSQL;

            final Map arg = new HashMap<>();
            jdbcTemplate.queryForRowSet("SELECT MESSAGE_ID,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX, MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=:idpk FOR UPDATE", withAny(arg));
            result = sqlRowSet;

            sqlRowSet.next();
            result = true;

            sqlRowSet.getString(1);
            result = messageId;

            sqlRowSet.getInt(2);
            result = sendAttempts;

            sqlRowSet.getInt(3);
            result = sendAttemptsMax;

            sqlRowSet.getObject(4);
            result = timestamp;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess((int) idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.EXPIRED, nextPullMessageToProcess.getState());
        assertNotNull(null, nextPullMessageToProcess.getStaledReason());



        new Verifications() {{
            Map<String, Object> params;
            jdbcTemplate.update("UPDATE TB_MESSAGING_LOCK ml SET ml.MESSAGE_STATE=:MESSAGE_STATE WHERE ml.ID_PK=:idpk", params = withCapture());
            assertEquals(6, params.get(MessagingLockDaoImpl.IDPK));
            assertEquals(MessageState.DEL.name(), params.get(MessagingLockDaoImpl.MESSAGE_STATE));

        }};
    }



    @Test
    public void getNextPullMessageToProcessNoMessage(@Mocked final SqlRowSet sqlRowSet) {
        final int idPk = 6;
        new Expectations() {{
            final Map arg = new HashMap<>();
            jdbcTemplate.queryForRowSet("SELECT MESSAGE_ID,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX, MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=:idpk FOR UPDATE", withAny(arg));
            result = sqlRowSet;

            sqlRowSet.next();
            result = false;
        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNull(nextPullMessageToProcess);
    }

    @Test
    public void delete(@Mocked final Query query) {
        final String messageId = "messageId";
        new Expectations() {{
            entityManager.createNamedQuery("MessagingLock.delete");
            result = query;
        }};
        messagingLockDao.delete(messageId);
        new Verifications() {{
            query.setParameter("MESSAGE_ID", messageId);
            query.executeUpdate();
        }};

    }
}