package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessagingLock;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@RunWith(JMockit.class)
public class MessagingLockDaoImplTest {

    @Injectable
    private NextPullMessageProcedure nextPullMessageProcedure;

    @Injectable
    private EntityManager entityManager;

    @Tested
    private MessagingLockDaoImpl messagingLockDao;

    @Test
    public void getNextPullMessageToProcess() {
        final String messageType = "messageType";
        final String initiator = "initiator";
        final String mpc = "mpc";

        messagingLockDao.getNextPullMessageToProcess(messageType, initiator, mpc);

        new Verifications(){{
            nextPullMessageProcedure.callProcedure(messageType, initiator, mpc);
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