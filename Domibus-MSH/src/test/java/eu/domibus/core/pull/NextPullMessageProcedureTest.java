package eu.domibus.core.pull;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;

@RunWith(JMockit.class)
public class NextPullMessageProcedureTest {

    @Injectable
    private EntityManager entityManager;

    @Tested
    private NextPullMessageProcedure nextPullMessageProcedure;

    @Test
    public void callProcedure(@Mocked final NextPullMessageProcedureWork storedProc,
                              @Mocked final Session unwrap) {

        new Expectations(){{
            entityManager.unwrap(Session.class);
            result=unwrap;
        }};

        nextPullMessageProcedure.callProcedure("messageType","initiator","mpc");

        new Verifications(){{
            unwrap.doWork(withAny(storedProc));
            storedProc.getMessageId();
        }};
    }
}