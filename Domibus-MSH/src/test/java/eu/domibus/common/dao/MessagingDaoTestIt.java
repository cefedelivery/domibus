package eu.domibus.common.dao;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.UserMessageLogBuilder;
import eu.domibus.ebms3.common.dao.DefaultDaoTestConfiguration;
import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.common.model.MessagePullDto;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by dussath on 6/1/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class MessagingDaoTestIt {

    static class ContextConfiguration extends DefaultDaoTestConfiguration {
        @Bean
        public MessagingDao messagingDao(){return new MessagingDao();}
        @Bean
        public PartyDao partyDao(){return new PartyDao();}
        @Bean
        public UserMessageLogDao userMessageLogDao(){return new UserMessageLogDao();}
    }

    @Autowired
    private MessagingDao messagingDao;
    @Autowired
    private PartyDao partyDao;
    @Autowired
    private UserMessageLogDao userMessageLogDao;
    @Test
    @Transactional
    @Rollback
    public void findMessagingOnStatusReceiverAndMpc() throws Exception {

       Party party = PojoInstaciatorUtil.instanciate(Party.class, " [name:blabla,identifiers{[partyId:testParty]}]");
        party.getIdentifiers().iterator().next().setPartyIdType(null);
        partyDao.create(party);
        Messaging firstMessage = PojoInstaciatorUtil.instanciate(Messaging.class, "userMessage[partyInfo[to[role:test,partyId{[value:testParty]}]]]");
        //firstMessage.setId("123456");
        MessageInfo messageInfo = firstMessage.getUserMessage().getMessageInfo();
        messageInfo.setRefToMessageId(null);
        messageInfo.setMessageId("123456");
        firstMessage.getUserMessage().setMpc("http://mpc");
        firstMessage.setId(null);

        Messaging secondMessage = PojoInstaciatorUtil.instanciate(Messaging.class, "userMessage[partyInfo[to[role:test,partyId{[value:testParty]}]]]");
        MessageInfo secondMessageInfo = secondMessage.getUserMessage().getMessageInfo();
        secondMessageInfo.setMessageId("789101212");
        secondMessageInfo.setRefToMessageId(null);
        secondMessage.setId(null);
        messagingDao.create(firstMessage);
        //@thom fix this late because their is a weird contraint exception here.
     //   messagingDao.create(secondMessage);
        UserMessageLogBuilder umlBuilder = UserMessageLogBuilder.create()
                .setMessageId(messageInfo.getMessageId())
                .setMessageStatus(MessageStatus.READY_TO_PULL)
                .setMshRole(MSHRole.SENDING)
                ;
        userMessageLogDao.create(umlBuilder.build());

        List<MessagePullDto> testParty = messagingDao.findMessagingOnStatusReceiverAndMpc(party.getEntityId(), MessageStatus.READY_TO_PULL,"http://mpc" );
        assertEquals(1,testParty.size());
        assertEquals("123456",testParty.get(0).getMessageId());
    }

}