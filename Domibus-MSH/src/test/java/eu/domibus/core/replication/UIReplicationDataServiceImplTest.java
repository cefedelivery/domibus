package eu.domibus.core.replication;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.UserMessageDefaultServiceHelper;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.OptimisticLockException;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * JUnit for {@link UIReplicationDataServiceImpl} class
 *
 * @author Catalin Enache
 * @since 4.0
 */
@RunWith(JMockit.class)
public class UIReplicationDataServiceImplTest {

    @Tested
    UIReplicationDataServiceImpl uiReplicationDataService;

    @Injectable
    private UIMessageDaoImpl uiMessageDao;

    @Injectable
    private UIMessageDiffDao uiMessageDiffDao;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private SignalMessageLogDao signalMessageLogDao;

    @Injectable
    private UserMessageDefaultServiceHelper userMessageDefaultServiceHelper;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainCoreConverter domainConverter;

    private ObjectFactory ebmsObjectFactory = new ObjectFactory();
    private final String messageId = UUID.randomUUID().toString();
    private final MessageStatus messageStatus = MessageStatus.SEND_ENQUEUED;
    private final NotificationStatus notificationStatus = NotificationStatus.REQUIRED;
    private final MSHRole mshRole = MSHRole.SENDING;
    private final MessageType messageType = MessageType.USER_MESSAGE;
    private final String conversationId = UUID.randomUUID().toString();
    private final String refToMessageId = UUID.randomUUID().toString();
    private Random rnd = new Random();
    private final Date deleted = new Date(Math.abs(System.currentTimeMillis() - rnd.nextLong()));
    private final Date received = new Date(Math.abs(System.currentTimeMillis() - rnd.nextLong()));
    private final Date nextAttempt = new Date(Math.abs(System.currentTimeMillis() - rnd.nextLong()));
    private final Date failed = new Date(Math.abs(System.currentTimeMillis() - rnd.nextLong()));
    private final Date restored = new Date(Math.abs(System.currentTimeMillis() - rnd.nextLong()));
    private final MessageSubtype messageSubtype = MessageSubtype.TEST;

    private final int sendAttempts = 1;
    private final int sendAttemptsMax = 5;

    private final String toPartyId = "domibus-red";
    private final String toPartyIdType = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private final String fromPartyId = "domibus-blue";
    private final String fromPartyIdType = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private final String originalSender = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
    private final String finalRecipient = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";


    @Test
    public void testMessageReceived(final @Mocked UIMessageEntity uiMessageEntity, final @Injectable Mapper domainCoreConverter) {

        new Expectations(uiReplicationDataService) {{
            uiReplicationDataService.saveUIMessageFromUserMessageLog(anyString);
        }};

        //tested
        uiReplicationDataService.messageReceived(messageId);

        new FullVerifications(uiReplicationDataService) {{
            String messageIdActual;
            uiReplicationDataService.saveUIMessageFromUserMessageLog(messageIdActual = withCapture());
            times = 1;
            Assert.assertEquals(messageId, messageIdActual);
        }};
    }

    @Test
    public void testMessageSubmitted() {
        new Expectations(uiReplicationDataService) {{
            uiReplicationDataService.saveUIMessageFromUserMessageLog(anyString);
        }};

        //tested
        uiReplicationDataService.messageSubmitted(messageId);

        new FullVerifications(uiReplicationDataService) {{
            String messageIdActual;
            uiReplicationDataService.saveUIMessageFromUserMessageLog(messageIdActual = withCapture());
            times = 1;
            Assert.assertEquals(messageId, messageIdActual);
        }};
    }

    @Test
    public void testMessageStatusChange_EntityFound_ResultOK(final @Mocked UIMessageEntity uiMessageEntity) {
        final UserMessageLog userMessageLog = createUserMessageLog();

        new Expectations(uiReplicationDataService) {{
            userMessageLogDao.findByMessageId(anyString);
            result = userMessageLog;

            uiMessageDao.findUIMessageByMessageId(anyString);
            result = uiMessageEntity;
        }};

        //tested method
        uiReplicationDataService.messageStatusChange(messageId);

        new FullVerifications(uiMessageEntity) {{
            MessageStatus messageStatusActual;
            uiMessageEntity.setMessageStatus(messageStatusActual = withCapture());
            Assert.assertEquals(messageStatus, messageStatusActual);

            Date dateActual;
            uiMessageEntity.setDeleted(dateActual = withCapture());
            Assert.assertEquals(deleted, dateActual);

            uiMessageEntity.setNextAttempt(dateActual = withCapture());
            Assert.assertEquals(nextAttempt, dateActual);

            uiMessageEntity.setFailed(dateActual = withCapture());
            Assert.assertEquals(failed, dateActual);

            String messageIdActual, operationName;
            uiReplicationDataService.updateAndFlush(messageIdActual = withCapture(), uiMessageEntity, operationName = withCapture());
            Assert.assertEquals(messageId, messageIdActual);
            Assert.assertEquals("messageStatusChange", operationName);

        }};
    }

    @Test
    public void testMessageStatusChange_EntityNotFound_Warning() {
        final UserMessageLog userMessageLog = createUserMessageLog();

        new Expectations() {{
            userMessageLogDao.findByMessageId(anyString);
            result = userMessageLog;

            uiMessageDao.findUIMessageByMessageId(anyString);
            result = null;
        }};

        //tested method
        uiReplicationDataService.messageStatusChange(messageId);

        new FullVerifications(uiReplicationDataService) {{

        }};
    }

    @Test
    public void testMessageNotificationStatusChange_EntityFound_ResultOK(final @Mocked UIMessageEntity uiMessageEntity) {
        final UserMessageLog userMessageLog = createUserMessageLog();

        new Expectations(uiReplicationDataService) {{
            userMessageLogDao.findByMessageId(anyString);
            result = userMessageLog;

            uiMessageDao.findUIMessageByMessageId(anyString);
            result = uiMessageEntity;
        }};

        //tested method
        uiReplicationDataService.messageNotificationStatusChange(messageId);

        new FullVerifications(uiReplicationDataService) {{
            NotificationStatus notificationStatusActual;
            uiMessageEntity.setNotificationStatus(notificationStatusActual = withCapture());
            Assert.assertEquals(notificationStatus, notificationStatusActual);

            String messageIdActual, operationName;
            uiReplicationDataService.updateAndFlush(messageIdActual = withCapture(), uiMessageEntity, operationName = withCapture());
            Assert.assertEquals(messageId, messageIdActual);
            Assert.assertEquals("messageNotificationStatusChange", operationName);
        }};
    }

    @Test
    public void testMessageNotificationStatusChange_EntityNotFound_Warning() {
        final UserMessageLog userMessageLog = createUserMessageLog();

        new Expectations(uiReplicationDataService) {{
            userMessageLogDao.findByMessageId(anyString);
            result = userMessageLog;

            uiMessageDao.findUIMessageByMessageId(anyString);
            result = null;
        }};

        //tested method
        uiReplicationDataService.messageNotificationStatusChange(messageId);

        new FullVerifications(uiReplicationDataService) {{

        }};
    }

    @Test
    public void test_MessageChange_EntityFound_ResultOK(final @Mocked UIMessageEntity uiMessageEntity) {
        final UserMessageLog userMessageLog = createUserMessageLog();

        new Expectations(uiReplicationDataService) {{
            userMessageLogDao.findByMessageId(anyString);
            result = userMessageLog;

            uiMessageDao.findUIMessageByMessageId(anyString);
            result = uiMessageEntity;
        }};

        //tested method
        uiReplicationDataService.messageChange(messageId);

        new FullVerifications(uiReplicationDataService) {{
            uiReplicationDataService.updateUIMessage(userMessageLog, uiMessageEntity);

            String messageIdActual, operationName;
            uiReplicationDataService.updateAndFlush(messageIdActual = withCapture(), uiMessageEntity, operationName = withCapture());
            Assert.assertEquals(messageId, messageIdActual);
            Assert.assertEquals("messageChange", operationName);
        }};
    }

    @Test
    public void messageChange_EntityNotFound_Warn() {
        final UserMessageLog userMessageLog = createUserMessageLog();

        new Expectations(uiReplicationDataService) {{
            userMessageLogDao.findByMessageId(anyString);
            result = userMessageLog;

            uiMessageDao.findUIMessageByMessageId(anyString);
            result = null;
        }};

        //tested method
        uiReplicationDataService.messageChange(messageId);

        new FullVerifications(uiReplicationDataService) {{
        }};
    }

    @Test
    public void testSignalMessageSubmitted() {

        new Expectations(uiReplicationDataService) {{
            uiReplicationDataService.saveUIMessageFromSignalMessageLog(anyString);
        }};

        //tested
        uiReplicationDataService.signalMessageSubmitted(messageId);

        new FullVerifications(uiReplicationDataService) {{
            String messageIdActual;
            uiReplicationDataService.saveUIMessageFromSignalMessageLog(messageIdActual = withCapture());
            times = 1;
            Assert.assertEquals(messageId, messageIdActual);
        }};
    }

    @Test
    public void testSignalMessageReceived() {
        new Expectations(uiReplicationDataService) {{
            uiReplicationDataService.saveUIMessageFromSignalMessageLog(anyString);
        }};

        //tested
        uiReplicationDataService.signalMessageSubmitted(messageId);

        new FullVerifications(uiReplicationDataService) {{
            String messageIdActual;
            uiReplicationDataService.saveUIMessageFromSignalMessageLog(messageIdActual = withCapture());
            times = 1;
            Assert.assertEquals(messageId, messageIdActual);
        }};
    }

    @Test
    public void test_FindAndSyncUIMessages(final @Mocked UIMessageDiffEntity uiMessageDiffEntity) {
        final int countAllRows = 10;
        final List<UIMessageDiffEntity> uiMessageDiffEntityList = Collections.singletonList(uiMessageDiffEntity);
        Deencapsulation.setField(uiReplicationDataService, "maxRowsToSync", 10000);

        new Expectations() {{
            uiMessageDiffDao.countAll();
            result = countAllRows;

            uiMessageDiffDao.findAll();
            result = uiMessageDiffEntityList;
        }};

        //tested method
        uiReplicationDataService.findAndSyncUIMessages();

        new FullVerifications(){{
            uiReplicationDataService.convertToUIMessageEntity(uiMessageDiffEntity);

            uiMessageDao.saveOrUpdate(withAny(new UIMessageEntity()));
        }};
    }

    @Test
    public void test_FindAndSyncUIMessagesWithLimit(final @Mocked UIMessageDiffEntity uiMessageDiffEntity) {
        final int countAllRows = 10;
        final int limit = 20;
        final List<UIMessageDiffEntity> uiMessageDiffEntityList = Collections.singletonList(uiMessageDiffEntity);
        Deencapsulation.setField(uiReplicationDataService, "maxRowsToSync", 10000);

        new Expectations() {{
            uiMessageDiffDao.countAll();
            result = countAllRows;

            uiMessageDiffDao.findAll(anyInt);
            result = uiMessageDiffEntityList;
        }};

        //tested method
        final int syncedRows = uiReplicationDataService.findAndSyncUIMessages(limit);
        Assert.assertEquals(1, syncedRows);

        new FullVerifications(){{
            int actualValue;
            uiMessageDiffDao.findAll(actualValue = withCapture());
            Assert.assertEquals(limit, actualValue);

            uiReplicationDataService.convertToUIMessageEntity(uiMessageDiffEntity);

            uiMessageDao.saveOrUpdate(withAny(new UIMessageEntity()));
        }};
    }

    @Test
    public void test_CountSyncUIMessages() {

        final int records = 214;
        new Expectations() {{
            uiMessageDiffDao.countAll();
            result = records;

        }};

        //tested method
        final int recordsToSync = uiReplicationDataService.countSyncUIMessages();
        Assert.assertEquals(records, recordsToSync);

        new FullVerifications() {{
            uiMessageDiffDao.countAll();
        }};
    }

    @Test
    public void testUpdateAndFlush_NoExceptionThrown_ResultOK(final @Mocked UIMessageEntity uiMessageEntity) {

        new Expectations() {{
        }};

        //tested method
        uiReplicationDataService.updateAndFlush(messageId, uiMessageEntity, "messageStatusChange");

        new FullVerifications() {{
            uiMessageDao.update(uiMessageEntity);
            uiMessageDao.flush();
        }};
    }

    //@Test
    public void testUpdateAndFlush_ExceptionThrown_Optimistic(final @Mocked UIMessageEntity uiMessageEntity) {

        new Expectations(uiReplicationDataService) {{
            uiMessageDao.flush();
            result = new OptimisticLockException("Exception raised");
        }};

        try {
            //tested method
            uiReplicationDataService.updateAndFlush(messageId, uiMessageEntity, "messageStatusChange");
            Assert.fail("exception expected");
        } catch (OptimisticLockException e) {

        }

        new Verifications() {{
            uiMessageDao.update(uiMessageEntity);
        }};
    }

    @Test
    public void testSaveUIMessageFromSignalMessageLog(final @Mocked UIMessageEntity uiMessageEntity) {
        final UserMessageLog userMessageLog = createUserMessageLog();
        final UserMessage userMessage = createUserMessage();

        new Expectations(uiReplicationDataService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            domainConverter.convert(userMessageLog, UIMessageEntity.class);
            result = uiMessageEntity;

            userMessageDefaultServiceHelper.getFinalRecipient(userMessage);
            result = finalRecipient;

            userMessageDefaultServiceHelper.getOriginalSender(userMessage);
            result = originalSender;

        }};

        //tested method
        uiReplicationDataService.saveUIMessageFromUserMessageLog(messageId);

        new FullVerifications(uiMessageEntity) {{
            final UIMessageEntity entityActual;
            uiMessageDao.create(entityActual = withCapture());
            Assert.assertNotNull(entityActual);

            String actualValue;
            uiMessageEntity.setMessageId(actualValue = withCapture());
            Assert.assertEquals(messageId, actualValue);

            uiMessageEntity.setConversationId(actualValue = withCapture());
            Assert.assertEquals(conversationId, actualValue);

            uiMessageEntity.setFromId(actualValue = withCapture());
            Assert.assertEquals(fromPartyId, actualValue);

            uiMessageEntity.setToId(actualValue = withCapture());
            Assert.assertEquals(toPartyId, actualValue);

            uiMessageEntity.setFromScheme(actualValue = withCapture());
            Assert.assertEquals(originalSender, actualValue);

            uiMessageEntity.setToScheme(actualValue = withCapture());
            Assert.assertEquals(finalRecipient, actualValue);

            uiMessageEntity.setRefToMessageId(actualValue = withCapture());
            Assert.assertEquals(refToMessageId, actualValue);

        }};

    }

    @Test
    public void testSaveUIMessageFromUserMessageLog(final @Mocked Messaging messaging, final @Mocked UIMessageEntity uiMessageEntity) {
        final SignalMessageLog signalMessageLog = createSignalMessageLog();
        final UserMessage userMessage = createUserMessage();
        final SignalMessage signalMessage = createSignalMessage();

        new Expectations(uiReplicationDataService) {{
            signalMessageLogDao.findByMessageId(messageId);
            result = signalMessageLog;

            messagingDao.findSignalMessageByMessageId(messageId);
            result = signalMessage;

            signalMessage.getMessageInfo().getRefToMessageId();
            result = refToMessageId;

            messagingDao.findMessageByMessageId(refToMessageId);
            result = messaging;

            messaging.getUserMessage();
            result = userMessage;

            domainConverter.convert(signalMessageLog, UIMessageEntity.class);
            result = uiMessageEntity;

            userMessageDefaultServiceHelper.getFinalRecipient(userMessage);
            result = finalRecipient;

            userMessageDefaultServiceHelper.getOriginalSender(userMessage);
            result = originalSender;

        }};

        //tested method
        uiReplicationDataService.saveUIMessageFromSignalMessageLog(messageId);

        new FullVerifications(uiMessageEntity) {{
            final UIMessageEntity entityActual;
            uiMessageDao.create(entityActual = withCapture());
            Assert.assertNotNull(entityActual);

            String actualValue;
            uiMessageEntity.setMessageId(actualValue = withCapture());
            Assert.assertEquals(messageId, actualValue);

            uiMessageEntity.setConversationId(actualValue = withCapture());
            Assert.assertEquals(StringUtils.EMPTY, actualValue);

            uiMessageEntity.setFromId(actualValue = withCapture());
            Assert.assertEquals(fromPartyId, actualValue);

            uiMessageEntity.setToId(actualValue = withCapture());
            Assert.assertEquals(toPartyId, actualValue);

            uiMessageEntity.setFromScheme(actualValue = withCapture());
            Assert.assertEquals(originalSender, actualValue);

            uiMessageEntity.setToScheme(actualValue = withCapture());
            Assert.assertEquals(finalRecipient, actualValue);

            uiMessageEntity.setRefToMessageId(actualValue = withCapture());
            Assert.assertEquals(refToMessageId, actualValue);

        }};
    }

    @Test
    public void testUpdateUIMessage(final @Mocked UIMessageEntity uiMessageEntity) {
        final UserMessageLog userMessageLog = createUserMessageLog();

        //tested method
        uiReplicationDataService.updateUIMessage(userMessageLog, uiMessageEntity);

        new FullVerifications(uiMessageEntity) {{
            MessageStatus messageStatusActual;
            uiMessageEntity.setMessageStatus(messageStatusActual = withCapture());
            Assert.assertEquals(messageStatus, messageStatusActual);

            Date dateActual;
            uiMessageEntity.setDeleted(dateActual = withCapture());
            Assert.assertEquals(deleted, dateActual);

            uiMessageEntity.setFailed(dateActual = withCapture());
            Assert.assertEquals(failed, dateActual);

            uiMessageEntity.setRestored(dateActual = withCapture());
            Assert.assertEquals(restored, dateActual);

            uiMessageEntity.setNextAttempt(dateActual = withCapture());
            Assert.assertEquals(nextAttempt, dateActual);

            int intActual;
            uiMessageEntity.setSendAttempts(intActual = withCapture());
            Assert.assertEquals(sendAttempts, intActual);

            uiMessageEntity.setSendAttemptsMax(intActual = withCapture());
            Assert.assertEquals(sendAttemptsMax, intActual);

            NotificationStatus notificationStatusActual;
            uiMessageEntity.setNotificationStatus(notificationStatusActual = withCapture());
            Assert.assertEquals(notificationStatus, notificationStatusActual);
        }};
    }

    @Test
    public void testConvertToUIMessageEntity_EntityNotNull_ResultOK(final @Mocked UIMessageDiffEntity uiMessageDiffEntity) {

        //tested method
        uiReplicationDataService.convertToUIMessageEntity(uiMessageDiffEntity);

        new Verifications() {{
            //just test the call to converter
            domainConverter.convert(uiMessageDiffEntity, UIMessageEntity.class);
            times = 1;
        }};
    }

    @Test
    public void testConvertToUIMessageEntity_EntityNull_ResultNull(final @Mocked UIMessageDiffEntity uiMessageDiffEntity) {

        //tested method
        final UIMessageEntity uiMessageEntity = uiReplicationDataService.convertToUIMessageEntity(uiMessageDiffEntity);
        Assert.assertNull(uiMessageEntity);
    }

    private UserMessageLog createUserMessageLog() {
        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageId(messageId);
        userMessageLog.setMessageStatus(messageStatus);
        userMessageLog.setNotificationStatus(notificationStatus);
        userMessageLog.setMshRole(mshRole);
        userMessageLog.setMessageType(messageType);
        userMessageLog.setDeleted(deleted);
        userMessageLog.setReceived(received);
        userMessageLog.setSendAttempts(sendAttempts);
        userMessageLog.setSendAttemptsMax(sendAttemptsMax);
        userMessageLog.setNextAttempt(nextAttempt);
        userMessageLog.setFailed(failed);
        userMessageLog.setRestored(restored);
        userMessageLog.setMessageSubtype(messageSubtype);

        return userMessageLog;
    }

    private SignalMessageLog createSignalMessageLog() {
        SignalMessageLog signalMessageLog = new SignalMessageLog();
        signalMessageLog.setMessageId(messageId);
        signalMessageLog.setMessageStatus(messageStatus);
        signalMessageLog.setNotificationStatus(notificationStatus);
        signalMessageLog.setMshRole(mshRole);
        signalMessageLog.setMessageType(messageType);
        signalMessageLog.setDeleted(deleted);
        signalMessageLog.setReceived(received);
        signalMessageLog.setSendAttempts(sendAttempts);
        signalMessageLog.setSendAttemptsMax(sendAttemptsMax);
        signalMessageLog.setNextAttempt(nextAttempt);
        signalMessageLog.setFailed(failed);
        signalMessageLog.setRestored(restored);
        signalMessageLog.setMessageSubtype(messageSubtype);

        return signalMessageLog;
    }

    private UserMessage createUserMessage() {
        UserMessage userMessage = ebmsObjectFactory.createUserMessage();
        MessageInfo messageInfo = ebmsObjectFactory.createMessageInfo();
        messageInfo.setMessageId(messageId);
        messageInfo.setRefToMessageId(refToMessageId);
        CollaborationInfo collaborationInfo = ebmsObjectFactory.createCollaborationInfo();
        collaborationInfo.setConversationId(conversationId);

        userMessage.setMessageInfo(messageInfo);
        userMessage.setCollaborationInfo(collaborationInfo);
        userMessage.setPartyInfo(createPartyInfo());
        userMessage.setMessageProperties(createMessageProperties());

        return userMessage;
    }

    private SignalMessage createSignalMessage() {
        SignalMessage signalMessage = ebmsObjectFactory.createSignalMessage();
        MessageInfo messageInfo = ebmsObjectFactory.createMessageInfo();
        messageInfo.setMessageId(messageId);
        messageInfo.setRefToMessageId(refToMessageId);
        signalMessage.setMessageInfo(messageInfo);

        return signalMessage;
    }

    private PartyInfo createPartyInfo() {
        PartyInfo partyInfo = ebmsObjectFactory.createPartyInfo();

        PartyId partyId = ebmsObjectFactory.createPartyId();
        partyId.setValue(toPartyId);
        partyId.setType((toPartyIdType));

        To to = ebmsObjectFactory.createTo();
        to.getPartyId().add(partyId);
        partyInfo.setTo(to);

        partyId = ebmsObjectFactory.createPartyId();
        partyId.setValue(fromPartyId);
        partyId.setType((fromPartyIdType));

        From from = ebmsObjectFactory.createFrom();
        from.getPartyId().add(partyId);
        partyInfo.setFrom(from);

        return partyInfo;
    }

    private MessageProperties createMessageProperties() {
        Property finalRecipientProp = new Property();
        finalRecipientProp.setName(MessageConstants.FINAL_RECIPIENT);
        finalRecipientProp.setValue(finalRecipient);

        Property originalSenderProp = new Property();
        originalSenderProp.setName(MessageConstants.ORIGINAL_SENDER);
        originalSenderProp.setValue(originalSender);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getProperty().add(finalRecipientProp);
        messageProperties.getProperty().add(originalSenderProp);

        return messageProperties;
    }
}