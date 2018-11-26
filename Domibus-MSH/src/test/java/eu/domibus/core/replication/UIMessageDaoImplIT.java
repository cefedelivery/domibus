package eu.domibus.core.replication;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.dao.InMemoryDataBaseConfig;
import eu.domibus.ebms3.common.model.MessageSubtype;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class, UIMessageDaoImplIT.UIReplicationConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
@Transactional
public class UIMessageDaoImplIT {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(UIReplicationConfig.class);

    @Autowired
    private UIMessageDaoImpl uiMessageDao;

    private final String messageId1 = UUID.randomUUID().toString();
    private final String messageId2 = UUID.randomUUID().toString();
    private final String messageId3 = UUID.randomUUID().toString();
    private final String conversationId = UUID.randomUUID().toString();

    private UIMessageEntity uiMessageEntity1, uiMessageEntity2, uiMessageEntity3;


    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Configuration
    static public class UIReplicationConfig {

        @Bean
        public UIMessageDaoImpl uiMessageDao() {
            return new UIMessageDaoImpl();
        }
    }


    @Before
    public void setUp() throws Exception {
        uiMessageEntity1 = createUIMessageEntity(messageId1, "domibus-blue", "domibus-red", MSHRole.SENDING);
        uiMessageEntity2 = createUIMessageEntity(messageId2, "domibus-blue", "domibus-red", MSHRole.SENDING);
        uiMessageEntity3 = createUIMessageEntity(messageId3, "domibus-red", "domibus-blue", MSHRole.RECEIVING);
    }


    private UIMessageEntity createUIMessageEntity(final String messageId, String fromId, String toId, MSHRole mshRole) {

        UIMessageEntity uiMessageEntity = new UIMessageEntity();
        uiMessageEntity.setMessageId(messageId);
        uiMessageEntity.setMessageStatus(MessageStatus.ACKNOWLEDGED);
        uiMessageEntity.setNotificationStatus(NotificationStatus.NOT_REQUIRED);
        uiMessageEntity.setMessageType(MessageType.USER_MESSAGE);
        uiMessageEntity.setMessageSubtype(null);
        uiMessageEntity.setFromId(fromId);
        uiMessageEntity.setFromScheme("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1");
        uiMessageEntity.setToId(toId);
        uiMessageEntity.setToScheme("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        uiMessageEntity.setReceived(new Date());
        uiMessageEntity.setConversationId(conversationId);
        uiMessageEntity.setMshRole(mshRole);
        uiMessageEntity.setSendAttempts(0);
        uiMessageEntity.setSendAttemptsMax(5);

        uiMessageDao.create(uiMessageEntity);

        return uiMessageEntity;
    }

    @Test
    public void testFindUIMessageByMessageId() {

        UIMessageEntity uiMessageEntity = uiMessageDao.findUIMessageByMessageId(messageId1);
        Assert.assertNotNull(uiMessageEntity);
        Assert.assertEquals(uiMessageEntity1, uiMessageEntity);

        uiMessageEntity = uiMessageDao.findUIMessageByMessageId(messageId2);
        Assert.assertNotNull(uiMessageEntity);
        Assert.assertEquals(uiMessageEntity2, uiMessageEntity);

        uiMessageEntity = uiMessageDao.findUIMessageByMessageId(messageId3);
        Assert.assertNotNull(uiMessageEntity);
        Assert.assertEquals(uiMessageEntity3, uiMessageEntity);

        Assert.assertNull(uiMessageDao.findUIMessageByMessageId(messageId1 + "123"));
    }

    @Test
    public void testCountMessages() {
        Map<String, Object> filters = new HashMap<>();
        int count;

        filters.put("messageSubtype", MessageSubtype.TEST);
        count = uiMessageDao.countMessages(filters);
        Assert.assertEquals(0, count);

        filters.put("messageSubtype", null);
        filters.put("fromPartyId", "domibus-blue");
        count = uiMessageDao.countMessages(filters);
        Assert.assertEquals(2, count);
    }

    @Test
    public void testFindPaged() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("mshRole", MSHRole.SENDING);
        filters.put("messageType", MessageType.USER_MESSAGE);

        List<UIMessageEntity> uiMessageEntityList = uiMessageDao.findPaged(0, 1, "received", true, filters);
        Assert.assertEquals(1, uiMessageEntityList.size());

        uiMessageEntityList = uiMessageDao.findPaged(1, 1, "received", true, filters);
        Assert.assertEquals(1, uiMessageEntityList.size());

    }

    @Test
    public void testSaveOrUpdate() {

        //save
        final String messageId = UUID.randomUUID().toString();
        UIMessageEntity uiMessageEntity = new UIMessageEntity();
        uiMessageEntity.setMessageId(messageId);
        uiMessageEntity.setMessageStatus(MessageStatus.ACKNOWLEDGED);
        uiMessageEntity.setNotificationStatus(NotificationStatus.NOT_REQUIRED);
        uiMessageEntity.setMessageType(MessageType.USER_MESSAGE);
        uiMessageEntity.setMessageSubtype(null);
        uiMessageEntity.setConversationId(conversationId);

        uiMessageDao.saveOrUpdate(uiMessageEntity);

        //update
        uiMessageEntity3.setSendAttempts(3);
        uiMessageDao.saveOrUpdate(uiMessageEntity3);
        Assert.assertEquals(3, uiMessageDao.findUIMessageByMessageId(messageId3).getSendAttempts());
    }

    @Test
    public void testUpdateMessageStatus() {

        Assert.assertTrue(uiMessageDao.updateMessageStatus(messageId1, MessageStatus.SEND_ENQUEUED, null,
                null, null, new Date()));

        Assert.assertFalse(uiMessageDao.updateMessageStatus(messageId1 + "123", MessageStatus.SEND_ENQUEUED, null,
                null, null, new Date()));
    }

    @Test
    public void testUpdateNotificationStatus() {
        Assert.assertTrue(uiMessageDao.updateNotificationStatus(messageId2, NotificationStatus.NOTIFIED, new Date()));

        Assert.assertFalse(uiMessageDao.updateNotificationStatus(messageId2 + "123", NotificationStatus.NOTIFIED, new Date()));
    }

    @Test
    public void testUpdateMessage() {

        Assert.assertTrue(uiMessageDao.updateMessage(messageId3, MessageStatus.DOWNLOADED, null, null, null,
                null, null, null, new Date()));
        Assert.assertFalse(uiMessageDao.updateMessage(messageId3 + "123", MessageStatus.DOWNLOADED, null, null, null,
                null, null, null, new Date()));

    }

}