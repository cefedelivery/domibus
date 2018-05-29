package eu.domibus.core.pull;

import com.google.common.collect.Lists;
import eu.domibus.api.configuration.DataBaseEngine;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.MessagingLock;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.sql.Timestamp;

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
    public void getNextPullMessageToProcessFirstAttempt(@Mocked final Query query, @Mocked final MessagingLock messagingLock) {
        final int idPk = 6;
        final String messageId = "firstTimeMessageId";

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.MYSQL;

            entityManager.createNativeQuery("SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED  FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=?1 FOR UPDATE", MessagingLock.class);
            result = query;

            query.getResultList();
            result = Lists.newArrayList(messagingLock);

            messagingLock.getMessageId();
            this.result = messageId;

            messagingLock.getStaled();
            this.result = new java.sql.Timestamp(System.currentTimeMillis() + 20000);

            messagingLock.getSendAttempts();
            this.result = 0;

            messagingLock.getSendAttemptsMax();
            this.result = 5;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.FIRST_ATTEMPT, nextPullMessageToProcess.getState());
        assertEquals(null, nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            messagingLock.setMessageState(MessageState.PROCESS);
            entityManager.persist(messagingLock);
        }};
    }

    @Test
    public void getNextPullMessageToProcessFurtherAttempt(@Mocked final Query query, @Mocked final MessagingLock messagingLock) {
        final int idPk = 6;
        final String messageId = "furtherAttemptMessageId";

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.MYSQL;

            entityManager.createNativeQuery("SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED  FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=?1 FOR UPDATE", MessagingLock.class);
            result = query;

            query.getResultList();
            result = Lists.newArrayList(messagingLock);

            messagingLock.getMessageId();
            this.result = messageId;

            messagingLock.getStaled();
            this.result = new java.sql.Timestamp(System.currentTimeMillis() + 20000);

            messagingLock.getSendAttempts();
            this.result = 1;

            messagingLock.getSendAttemptsMax();
            this.result = 5;

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess((int) idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.RETRY, nextPullMessageToProcess.getState());
        assertEquals(null, nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            query.setParameter(1, idPk);
            messagingLock.setMessageState(MessageState.PROCESS);
            entityManager.persist(messagingLock);

        }};
    }

    @Test
    public void getNextPullMessageToProcessStaled(@Mocked final Query query, @Mocked final MessagingLock messagingLock) {
        final int idPk = 6;
        final String messageId = "staledMessageId";
        final Timestamp staledDate = new Timestamp(System.currentTimeMillis() - 20000);

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.MYSQL;

            entityManager.createNativeQuery("SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED  FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=?1 FOR UPDATE", MessagingLock.class);
            result = query;

            query.getResultList();
            result = Lists.newArrayList(messagingLock);

            messagingLock.getMessageId();
            this.result = messageId;

            messagingLock.getStaled();
            this.result = staledDate;

            messagingLock.getSendAttempts();
            this.result = 1;

            messagingLock.getSendAttemptsMax();
            this.result = 5;


        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.EXPIRED, nextPullMessageToProcess.getState());
        assertEquals(String.format("Maximum time to send the message has been reached:[%tc]", staledDate), nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            messagingLock.setMessageState(MessageState.DEL);
            entityManager.persist(messagingLock);
        }};
    }

    @Test
    public void getNextPullMessageToProcessMaxAttemptsReached(@Mocked final Query query, @Mocked final MessagingLock messagingLock) {
        final int idPk = 6;
        final String messageId = "maxAttemptMessageId";
        final Timestamp staledDate = new Timestamp(System.currentTimeMillis() + 20000);

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.H2;


            entityManager.createNativeQuery("SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED  FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=?1 FOR UPDATE", MessagingLock.class);
            result = query;

            query.getResultList();
            result = Lists.newArrayList(messagingLock);

            messagingLock.getMessageId();
            this.result = messageId;

            messagingLock.getStaled();
            this.result = staledDate;

            messagingLock.getSendAttempts();
            this.result = 5;

            messagingLock.getSendAttemptsMax();
            this.result = 5;


        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.EXPIRED, nextPullMessageToProcess.getState());
        assertEquals(String.format("Maximum number of attempts to send the message has been reached:[%d]", 5), nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            messagingLock.setMessageState(MessageState.DEL);
            entityManager.persist(messagingLock);
        }};
    }

    @Test
    public void getNextPullMessageToProcessNoMessage(@Mocked final Query query) {
        final int idPk = 6;
        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.ORACLE;


            entityManager.createNativeQuery("SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=?1 FOR UPDATE NOWAIT", MessagingLock.class);
            result = query;

            query.getResultList();
            result = Lists.newArrayList();


        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNull(nextPullMessageToProcess);
    }

    @Test(expected = PersistenceException.class)
    public void getNextPullMessageToProcessMessageLocked(@Mocked final Query query) {
        final int idPk = 6;
        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.ORACLE;

            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.ORACLE;


            entityManager.createNativeQuery("SELECT ID_PK,MESSAGE_TYPE,MESSAGE_RECEIVED,MESSAGE_STATE,MESSAGE_ID,INITIATOR,MPC,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,MESSAGE_STALED FROM TB_MESSAGING_LOCK ml where ml.MESSAGE_STATE='READY' and ml.ID_PK=?1 FOR UPDATE NOWAIT", MessagingLock.class);
            result = query;

            query.getResultList();
            result = new PersistenceException("test", null);

        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(idPk);
        assertNull(nextPullMessageToProcess);


        new Verifications() {{
            query.setParameter(1, idPk);
        }};
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