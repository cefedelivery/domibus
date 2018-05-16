package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessagingLock;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@RunWith(JMockit.class)
public class MessagingLockDaoImplTest {


    @Injectable
    private EntityManager entityManager;

    @Tested
    private MessagingLockDaoImpl messagingLockDao;

    @Test
    public void getNextPullMessageToProcess() {
        final String messageType = "messageType";
        final String initiator = "initiator";
        final String mpc = "mpc";
        final long idPk = 6;

        messagingLockDao.getNextPullMessageToProcess(idPk);

        new Verifications(){{
            Assert.assertTrue(false);

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