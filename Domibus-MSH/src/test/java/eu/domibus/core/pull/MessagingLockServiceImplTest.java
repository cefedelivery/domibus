package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.MessagingLock;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class MessagingLockServiceImplTest {

    @Injectable
    private MessagingLockDao messagingLockDao;

    @Tested
    private MessagingLockServiceImpl messagingLockService;

    private final static String MESSAGE_ID ="MESSAGE_ID";

    private final static String MPC = "MPC";

    @Test
    public void getPullMessageId() {
        final String initiator = "initiator";


        new Expectations() {
            {
                messagingLockDao.getNextPullMessageToProcess(MessagingLock.PULL, initiator, MPC);
                result= MESSAGE_ID;
            }
        };
        String pullMessageId = messagingLockService.getPullMessageId(initiator, MPC);
        assertEquals(MESSAGE_ID,pullMessageId);
    }

    @Test
    public void addSearchInFormation(@Mocked final PartyIdExtractor partyIdExtractor) {

        final String partyId="partyId";
        new Expectations() {
            {
                partyIdExtractor.getPartyId();
                result=partyId;
            }
        };
        messagingLockService.addSearchInFormation(partyIdExtractor, MESSAGE_ID, MPC);
        new Verifications(){{
            MessagingLock messagingLock;
           messagingLockDao.save(messagingLock=withCapture());
           assertEquals(MESSAGE_ID,messagingLock.getMessageId());
           assertEquals(MessageState.READY,messagingLock.getMessageState());
           assertEquals(MessagingLock.PULL,messagingLock.getMessageType());
           assertEquals(MPC,messagingLock.getMpc());
        }};
    }

    @Test
    public void delete() {
        messagingLockService.delete(MESSAGE_ID);
        new Verifications(){{
            messagingLockDao.delete(MESSAGE_ID);
        }};
    }
}