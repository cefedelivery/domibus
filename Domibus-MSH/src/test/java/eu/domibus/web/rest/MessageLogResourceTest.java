package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.PartyDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessagesLogService;
import eu.domibus.core.csv.CsvService;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.replication.UIMessageDao;
import eu.domibus.core.replication.UIMessageService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.MessageSubtype;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(Parameterized.class)
public class MessageLogResourceTest {

    private static final String CSV_TITLE = "Conversation Id, From Party Id, To Party Id, Original Sender, Final Recipient, ref To Message Id, Message Id, Message Status, Notification Status, " +
            "MSH Role, Message Type, Deleted, Received, Send Attempts, Send Attempts Max, Next Attempt, Failed, Restored, Message Subtype";

    @Tested
    MessageLogResource messageLogResource;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    PModeProvider pModeProvider;

    // needed Injectable
    @Injectable
    MessagingDao messagingDao;

    // needed Injectable
    @Injectable
    PartyDao partyDao;

    // needed Injectable
    @Injectable
    DateUtil dateUtil;

    // needed Injectable
    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private UIMessageService uiMessageService;

    @Injectable
    private UIMessageDao uiMessageDao;

    @Injectable
    private MessagesLogService messagesLogService;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Parameterized.Parameter(0)
    public MessageType messageType;

    @Parameterized.Parameter(1)
    public MessageLog messageLog;

    @Parameterized.Parameter(2)
    public MessageSubtype messageSubtype;

    @Parameterized.Parameter(3)
    public boolean useFlatTable;

    @Mocked
    SignalMessage signalMessage;

    @Parameterized.Parameters(name = "{index}: messageType=\"{0}\" messageSubtype=\"{2}\"")
    public static Collection<Object[]> values() {
        return Arrays.asList(new Object[][]{
                {MessageType.USER_MESSAGE, new UserMessageLog(), null, false},
                {MessageType.USER_MESSAGE, new UserMessageLog(), MessageSubtype.TEST, false},
                {MessageType.SIGNAL_MESSAGE, new SignalMessageLog(), null, false},
                {MessageType.SIGNAL_MESSAGE, new SignalMessageLog(), MessageSubtype.TEST, false},
        });
    }

    @Test
    public void testMessageLog() {
        // Given
        final MessageLogRO messageLogRO = createMessageLog(messageType, messageSubtype);
        final List<MessageLogRO> resultList = Collections.singletonList(messageLogRO);
        MessageLogResultRO expectedMessageLogResult = new MessageLogResultRO();
        expectedMessageLogResult.setMessageLogEntries(resultList);

        new Expectations() {{
            messagesLogService.countAndFindPaged(messageType, anyInt, anyInt, anyString, anyBoolean, (HashMap<String, Object>) any);
            result = expectedMessageLogResult;
        }};

        // When
        final MessageLogResultRO messageLogResultRO = getMessageLog(messageType, messageSubtype);

        // Then
        Assert.assertNotNull(messageLogResultRO);
        Assert.assertEquals(1, messageLogResultRO.getMessageLogEntries().size());

        MessageLogRO actualMessageLogRO = messageLogResultRO.getMessageLogEntries().get(0);
        Assert.assertEquals(messageLogRO.getMessageId(), actualMessageLogRO.getMessageId());
        Assert.assertEquals(messageLogRO.getMessageStatus(), actualMessageLogRO.getMessageStatus());
        Assert.assertEquals(messageLogRO.getMessageType(), actualMessageLogRO.getMessageType());
        Assert.assertEquals(messageLogRO.getDeleted(), actualMessageLogRO.getDeleted());
        Assert.assertEquals(messageLogRO.getMshRole(), actualMessageLogRO.getMshRole());
        Assert.assertEquals(messageLogRO.getNextAttempt(), actualMessageLogRO.getNextAttempt());
        Assert.assertEquals(messageLogRO.getNotificationStatus(), actualMessageLogRO.getNotificationStatus());
        Assert.assertEquals(messageLogRO.getReceived(), actualMessageLogRO.getReceived());
        Assert.assertEquals(messageLogRO.getSendAttempts(), actualMessageLogRO.getSendAttempts());
        Assert.assertEquals(messageLogRO.getMessageSubtype(), actualMessageLogRO.getMessageSubtype());
    }

    @Test
    public void testMessageLogInfoGetCsv() throws CsvException {
        // Given
        Date date = new Date();
        List<MessageLogInfo> messageList = getMessageList(messageType, date, messageSubtype);

        new Expectations() {{
            domibusPropertyProvider.getProperty("domibus.ui.maximumcsvrows", anyString);
            result = CsvService.MAX_NUMBER_OF_ENTRIES;

            messagesLogService.findAllInfoCSV(messageType, anyInt, "received", true, (HashMap<String, Object>) any);
            result = messageList;

            csvServiceImpl.exportToCSV(messageList, null, (Map<String, String>)any, (List<String>)any);
            result = CSV_TITLE +
                    "conversationId,fromPartyId,toPartyId,originalSender,finalRecipient,refToMessageId,messageId," + MessageStatus.ACKNOWLEDGED + "," +
                    NotificationStatus.NOTIFIED + "," + MSHRole.RECEIVING + "," + messageType + "," + date + "," + date + ",1,5," + date + "," +
                    date + "," + date + "," + messageSubtype + System.lineSeparator();
        }};

        // When
        final ResponseEntity<String> csv = messageLogResource.getCsv("received", true, null, null, null, messageType, null,
                null, null, null, null, null,
                null, null, null, messageSubtype);

        // Then
        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals(CSV_TITLE +
                        "conversationId,fromPartyId,toPartyId,originalSender,finalRecipient,refToMessageId,messageId," + MessageStatus.ACKNOWLEDGED + "," + NotificationStatus.NOTIFIED + "," +
                        MSHRole.RECEIVING + "," + messageType + "," + date + "," + date + ",1,5," + date + "," + date + "," + date + "," + messageSubtype + System.lineSeparator(),
                csv.getBody());
    }

    @Test
    public void testUserMessageGetCsv_Exception() throws CsvException {
        // Given
        new Expectations() {{
            domibusPropertyProvider.getProperty("domibus.ui.maximumcsvrows", anyString);
            result = CsvService.MAX_NUMBER_OF_ENTRIES;
            csvServiceImpl.exportToCSV((List<?>) any, null, null, null);
            result = new CsvException(DomibusCoreErrorCode.DOM_001, "Exception", new Exception());
        }};

        // When
        final ResponseEntity<String> csv = messageLogResource.getCsv("received", true, null, null, null, messageType, null,
                null, null, null, null, null,
                null, null, null, messageSubtype);

        // Then
        Assert.assertEquals(HttpStatus.NO_CONTENT, csv.getStatusCode());
    }

    @Test
    public void testGetLastTestSent() {
        // Given
        String partyId = "test";
        String userMessageId = "testmessageid";
        UserMessageLog userMessageLog = new UserMessageLog();
        new Expectations() {{
            userMessageLogDao.findLastUserTestMessageId(partyId);
            result = userMessageId;
            userMessageLogDao.findByMessageId(userMessageId);
            result = userMessageLog;
        }};

        // When
        ResponseEntity<TestServiceMessageInfoRO> lastTestSent = messageLogResource.getLastTestSent(partyId);

        // Then
        TestServiceMessageInfoRO testServiceMessageInfoRO = lastTestSent.getBody();
        Assert.assertEquals(partyId, testServiceMessageInfoRO.getPartyId());
        Assert.assertEquals(userMessageId, testServiceMessageInfoRO.getMessageId());
    }

    @Test
    public void testGetLastTestSent_NotFound() {
        // Given
        new Expectations() {{
            userMessageLogDao.findLastUserTestMessageId(anyString);
            result = null;
        }};

        // When
        ResponseEntity<TestServiceMessageInfoRO> lastTestSent = messageLogResource.getLastTestSent("test");

        // Then
        Assert.assertEquals(HttpStatus.NO_CONTENT, lastTestSent.getStatusCode());
    }

    @Test
    public void testGetLastTestReceived(@Injectable Messaging messaging) {
        // Given
        String partyId = "partyId";
        String userMessageId = "userMessageId";

        Party party = new Party();
        party.setEndpoint("testEndpoint");

        new Expectations() {{
            messagingDao.findMessageByMessageId(anyString);
            result = messaging;
            messaging.getSignalMessage();
            result = signalMessage;
            pModeProvider.getPartyByIdentifier(partyId);
            result = party;
        }};

        // When
        ResponseEntity<TestServiceMessageInfoRO> lastTestReceived = messageLogResource.getLastTestReceived(partyId, userMessageId);

        // Then
        TestServiceMessageInfoRO testServiceMessageInfoRO = lastTestReceived.getBody();
        Assert.assertEquals(testServiceMessageInfoRO.getMessageId(), signalMessage.getMessageInfo().getMessageId());
        Assert.assertEquals(testServiceMessageInfoRO.getPartyId(), partyId);
        Assert.assertEquals(testServiceMessageInfoRO.getTimeReceived(), signalMessage.getMessageInfo().getTimestamp());
        Assert.assertEquals(testServiceMessageInfoRO.getAccessPoint(), party.getEndpoint());
    }

    @Test
    public void testGetLastTestReceived_NotFound() {
        // Given
        new Expectations() {{
            messagingDao.findMessageByMessageId(anyString);
            result = null;
        }};

        // When
        ResponseEntity<TestServiceMessageInfoRO> lastTestReceived = messageLogResource.getLastTestReceived("test", "test");

        // Then
        Assert.assertEquals(HttpStatus.NO_CONTENT, lastTestReceived.getStatusCode());
    }

    /**
     * Creates a {@link MessageLogRO} based on <code>messageType</code> and <code>messageSubtype</code>
     *
     * @param messageType    Message Type
     * @param messageSubtype Message Subtype
     * @return <code>MessageLog</code>
     */
    private static MessageLogRO createMessageLog(MessageType messageType, MessageSubtype messageSubtype) {

        MessageLogRO messageLogRO = new MessageLogRO();
        messageLogRO.setMessageId("messageId");
        messageLogRO.setMessageStatus(MessageStatus.ACKNOWLEDGED);
        messageLogRO.setNotificationStatus(NotificationStatus.REQUIRED);
        messageLogRO.setMshRole(MSHRole.RECEIVING);
        messageLogRO.setMessageType(messageType);
        messageLogRO.setDeleted(new Date());
        messageLogRO.setReceived(new Date());
        messageLogRO.setFromPartyId("fromPartyId");
        messageLogRO.setToPartyId("toPartyId");
        messageLogRO.setConversationId("conversationId");
        messageLogRO.setOriginalSender("originalSender");
        messageLogRO.setFinalRecipient("finalRecipient");
        messageLogRO.setRefToMessageId("refToMessageId");
        messageLogRO.setMessageSubtype(messageSubtype);


        return messageLogRO;
    }

    /**
     * Gets a MessageLog based on <code>messageType</code> and <code>messageSubtype</code>
     *
     * @param messageType    Message Type
     * @param messageSubtype Message Subtype
     * @return <code>MessageLogResultRO</code> object
     */
    private MessageLogResultRO getMessageLog(MessageType messageType, MessageSubtype messageSubtype) {
        return messageLogResource.getMessageLog(1, 10, "MessageId", true,
                null, null, null, messageType, null,
                null, null, null, null, null, null,
                null, null, messageSubtype);
    }

    /**
     * Get a MessageLogInfo List based on <code>messageInfo</code>, <code>date</code> and <code>messageSubtype</code>
     *
     * @param messageType    Message Type
     * @param date           Date
     * @param messageSubtype Message Subtype
     * @return <code>List</code> of <code>MessageLogInfo</code> objects
     */
    private List<MessageLogInfo> getMessageList(MessageType messageType, Date date, MessageSubtype messageSubtype) {
        List<MessageLogInfo> result = new ArrayList<>();
        MessageLogInfo messageLog = new MessageLogInfo("messageId", MessageStatus.ACKNOWLEDGED,
                NotificationStatus.NOTIFIED, MSHRole.RECEIVING, messageType, date, date, 1, 5, date,
                "conversationId", "fromPartyId", "toPartyId", "originalSender", "finalRecipient",
                "refToMessageId", date, date, messageSubtype);
        result.add(messageLog);
        return result;
    }
}
