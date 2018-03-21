package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.CsvService;
import eu.domibus.common.services.impl.CsvServiceImpl;
import eu.domibus.ebms3.common.model.MessageSubtype;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import mockit.Expectations;
import mockit.Injectable;
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
    SignalMessageLogDao signalMessageLogDao;

    // needed Injectable
    @Injectable
    DateUtil dateUtil;

    // needed Injectable
    @Injectable
    CsvServiceImpl csvServiceImpl;

    // needed Injectable
    @Injectable
    Properties domibusProperties;

    @Parameterized.Parameter(0)
    public MessageType messageType;

    @Parameterized.Parameter(1)
    public MessageLog messageLog;

    @Parameterized.Parameter(2)
    public MessageSubtype messageSubtype;

    @Parameterized.Parameters(name = "{index}: messageType=\"{0}\" messageSubtype=\"{2}\"")
    public static Collection<Object[]> values() {
        return Arrays.asList(new Object[][]{
                {MessageType.USER_MESSAGE, new UserMessageLog(), null},
                {MessageType.USER_MESSAGE, new UserMessageLog(), MessageSubtype.TEST},
                {MessageType.SIGNAL_MESSAGE, new SignalMessageLog(), null},
                {MessageType.SIGNAL_MESSAGE, new SignalMessageLog(), MessageSubtype.TEST},
        });
    }

    @Test
    public void testMessageLog() {
        // Given
        MessageLog createdMessageLog = createMessageLog(messageType, messageLog, messageSubtype);
        final MessageLogInfo messageLogInfo = new MessageLogInfo(
                createdMessageLog.getMessageId(),
                createdMessageLog.getMessageStatus(),
                createdMessageLog.getNotificationStatus(),
                createdMessageLog.getMshRole(),
                createdMessageLog.getMessageType(),
                createdMessageLog.getDeleted(),
                createdMessageLog.getReceived(),
                createdMessageLog.getSendAttempts(),
                createdMessageLog.getSendAttemptsMax(),
                createdMessageLog.getNextAttempt(),
                "conversationId",
                "fromPartyId",
                "toPartyId",
                "originalSender",
                "finalRecipient",
                "refToMessageId",
                createdMessageLog.getFailed(),
                createdMessageLog.getRestored(),
                createdMessageLog.getMessageSubtype());
        final List<MessageLogInfo> resultList = new ArrayList<>();
        resultList.add(messageLogInfo);
        // Expectations doesn't allow if's inside
        if(messageType.equals(MessageType.USER_MESSAGE)) {
            new Expectations() {{
                userMessageLogDao.findAllInfoPaged(anyInt, anyInt, anyString, anyBoolean, (HashMap<String, Object>) any);
                result = resultList;
            }};
        } else if (messageType.equals(MessageType.SIGNAL_MESSAGE)) {
            new Expectations() {{
                signalMessageLogDao.findAllInfoPaged(anyInt, anyInt, anyString, anyBoolean, (HashMap<String, Object>) any);
                result = resultList;
            }};
        }

        // When
        final MessageLogResultRO messageLogResultRO = getMessageLog(messageType, messageSubtype);

        // Then
        Assert.assertNotNull(messageLogResultRO);
        Assert.assertEquals(1, messageLogResultRO.getMessageLogEntries().size());

        MessageLogRO messageLogRO = messageLogResultRO.getMessageLogEntries().get(0);
        Assert.assertEquals(createdMessageLog.getMessageId(), messageLogRO.getMessageId());
        Assert.assertEquals(createdMessageLog.getMessageStatus(), messageLogRO.getMessageStatus());
        Assert.assertEquals(createdMessageLog.getMessageType(), messageLogRO.getMessageType());
        Assert.assertEquals(createdMessageLog.getDeleted(), messageLogRO.getDeleted());
        Assert.assertEquals(createdMessageLog.getMshRole(), messageLogRO.getMshRole());
        Assert.assertEquals(createdMessageLog.getNextAttempt(), messageLogRO.getNextAttempt());
        Assert.assertEquals(createdMessageLog.getNotificationStatus(), messageLogRO.getNotificationStatus());
        Assert.assertEquals(createdMessageLog.getReceived(), messageLogRO.getReceived());
        Assert.assertEquals(createdMessageLog.getSendAttempts(), messageLogRO.getSendAttempts());
        Assert.assertEquals(createdMessageLog.getMessageSubtype(), messageLogRO.getMessageSubtype());
    }

    @Test
    public void testMessageLogInfoGetCsv() throws CsvException {
        // Given
        Date date = new Date();
        List<MessageLogInfo> messageList = getMessageList(messageType, date, messageSubtype);

        // Expectations doesn't allow if's inside
        if(messageType.equals(MessageType.USER_MESSAGE)) {
            new Expectations() {{
                domibusProperties.getProperty("domibus.ui.maximumcsvrows", anyString);
                result = CsvService.MAX_NUMBER_OF_ENTRIES;
                userMessageLogDao.findAllInfoPaged(anyInt, anyInt, anyString, anyBoolean, (HashMap<String, Object>) any);
                result = messageList;
                csvServiceImpl.exportToCSV(messageList);
                result = CSV_TITLE +
                        "conversationId,fromPartyId,toPartyId,originalSender,finalRecipient,refToMessageId,messageId," + MessageStatus.ACKNOWLEDGED + "," +
                        NotificationStatus.NOTIFIED + "," + MSHRole.RECEIVING + "," + messageType + "," + date + "," + date + ",1,5," + date + "," +
                        date + "," + date + "," + messageSubtype + System.lineSeparator();
            }};
        } else if(messageType.equals(MessageType.SIGNAL_MESSAGE)) {
            new Expectations() {{
                domibusProperties.getProperty("domibus.ui.maximumcsvrows", anyString);
                result = CsvService.MAX_NUMBER_OF_ENTRIES;
                signalMessageLogDao.findAllInfoPaged(anyInt, anyInt, anyString, anyBoolean, (HashMap<String, Object>) any);
                result = messageList;
                csvServiceImpl.exportToCSV(messageList);
                result = CSV_TITLE +
                        "conversationId,fromPartyId,toPartyId,originalSender,finalRecipient,refToMessageId,messageId," + MessageStatus.ACKNOWLEDGED + "," +
                        NotificationStatus.NOTIFIED + "," + MSHRole.RECEIVING + "," + messageType + "," + date + "," + date + ",1,5," + date + "," +
                        date + "," + date + "," + messageSubtype + System.lineSeparator();
            }};
        }

        // When
        final ResponseEntity<String> csv = messageLogResource.getCsv(null, null, null, messageType, null,
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
            domibusProperties.getProperty("domibus.ui.maximumcsvrows", anyString);
            result = CsvService.MAX_NUMBER_OF_ENTRIES;
            csvServiceImpl.exportToCSV((List<?>) any);
            result = new CsvException(DomibusCoreErrorCode.DOM_001, "Exception", new Exception());
        }};

        // When
        final ResponseEntity<String> csv = messageLogResource.getCsv(null, null, null, messageType, null,
                null, null, null, null, null,
                null, null, null, messageSubtype);

        // Then
        Assert.assertEquals(HttpStatus.NO_CONTENT, csv.getStatusCode());
    }

    /**
     * Creates a <code>messageLog</code> based on <code>messageType</code> and <code>messageSubtype</code>
     * @param messageType Message Type
     * @param messageLog Message Log
     * @param messageSubtype Message Subtype
     * @return <code>MessageLog</code>
     */
    private static MessageLog createMessageLog(MessageType messageType, MessageLog messageLog, MessageSubtype messageSubtype) {
        messageLog.setEntityId(1);
        messageLog.setBackend("backend");
        messageLog.setDeleted(new Date());
        messageLog.setDownloaded(new Date());
        messageLog.setEndpoint("endpoint");
        messageLog.setFailed(new Date());
        messageLog.setMessageId("messageId");
        messageLog.setMessageStatus(MessageStatus.ACKNOWLEDGED);
        messageLog.setMessageType(messageType);
        messageLog.setMessageSubtype(messageSubtype);
        messageLog.setMpc("mpc");
        messageLog.setMshRole(MSHRole.RECEIVING);
        messageLog.setNextAttempt(new Date());
        messageLog.setReceived(new Date());
        messageLog.setSendAttemptsMax(4);
        return messageLog;
    }

    /**
     * Gets a MessageLog based on <code>messageType</code> and <code>messageSubtype</code>
     * @param messageType Message Type
     * @param messageSubtype Message Subtype
     * @return <code>MessageLogResultRO</code> object
     */
    private MessageLogResultRO getMessageLog(MessageType messageType, MessageSubtype messageSubtype) {
        return messageLogResource.getMessageLog(1, 10, 10, "MessageId", true,
                null, null, null, messageType, null,
                null, null, null, null, null, null,
                null, null, messageSubtype);
    }

    /**
     * Get a MessageLogInfo List based on <code>messageInfo</code>, <code>date</code> and <code>messageSubtype</code>
     * @param messageType Message Type
     * @param date Date
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
