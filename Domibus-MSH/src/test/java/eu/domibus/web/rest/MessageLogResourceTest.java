package eu.domibus.web.rest;

import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageLogResourceTest {

    @Tested
    MessageLogResource messageLogResource;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    SignalMessageLogDao signalMessageLogDao;

    @Injectable
    DateUtil dateUtil;

    /**
     * Creates a UserMessageLof or SignalMessageLog depending on boolean argument
     * @param user true if UserMessageLog, false if SignalMessageLog
     * @return
     */
    public static MessageLog createMessageLog(boolean user) {
        MessageLog messageLog = user ? new UserMessageLog() : new SignalMessageLog();
        messageLog.setEntityId(1);
        messageLog.setBackend("backend");
        messageLog.setDeleted(new Date());
        messageLog.setDownloaded(new Date());
        messageLog.setEndpoint("endpoint");
        messageLog.setFailed(new Date());
        messageLog.setMessageId("messageId");
        messageLog.setMessageStatus(MessageStatus.ACKNOWLEDGED);
        messageLog.setMessageType(user ? MessageType.USER_MESSAGE : MessageType.SIGNAL_MESSAGE);
        messageLog.setMpc("mpc");
        messageLog.setMshRole(MSHRole.RECEIVING);
        messageLog.setNextAttempt(new Date());
        messageLog.setReceived(new Date());
        messageLog.setSendAttemptsMax(4);
        return messageLog;
    }

    @Test
    public void testUserMessageLog() {
        // Given
        UserMessageLog userMessageLog = (UserMessageLog) createMessageLog(true);
        final MessageLogInfo messageLogInfo = new MessageLogInfo(
                userMessageLog.getMessageId(),
                userMessageLog.getMessageStatus(),
                userMessageLog.getNotificationStatus(),
                userMessageLog.getMshRole(),
                userMessageLog.getMessageType(),
                userMessageLog.getDeleted(),
                userMessageLog.getReceived(),
                userMessageLog.getSendAttempts(),
                userMessageLog.getSendAttemptsMax(),
                userMessageLog.getNextAttempt(),
                "conversationId",
                "fromPartyId",
                "toPartyId",
                "originalSender",
                "finalRecipient",
                "refToMessageId",
                userMessageLog.getFailed(),
                userMessageLog.getRestored());
        final List<MessageLogInfo> resultList = new ArrayList<>();
        resultList.add(messageLogInfo);
        new Expectations() {{
            userMessageLogDao.findAllInfoPaged(anyInt, anyInt, anyString, anyBoolean, (HashMap<String, Object>) any);
            result = resultList;
        }};

        // When
        final MessageLogResultRO messageLogResultRO = getMessageLog(MessageType.USER_MESSAGE);

        // Then
        Assert.assertNotNull(messageLogResultRO);
        Assert.assertEquals(1, messageLogResultRO.getMessageLogEntries().size());

        MessageLogRO messageLogRO = messageLogResultRO.getMessageLogEntries().get(0);
        Assert.assertEquals(userMessageLog.getMessageId(), messageLogRO.getMessageId());
        Assert.assertEquals(userMessageLog.getMessageStatus(), messageLogRO.getMessageStatus());
        Assert.assertEquals(userMessageLog.getMessageType(), messageLogRO.getMessageType());
        Assert.assertEquals(userMessageLog.getDeleted(), messageLogRO.getDeleted());
        Assert.assertEquals(userMessageLog.getMshRole(), messageLogRO.getMshRole());
        Assert.assertEquals(userMessageLog.getNextAttempt(), messageLogRO.getNextAttempt());
        Assert.assertEquals(userMessageLog.getNotificationStatus(), messageLogRO.getNotificationStatus());
        Assert.assertEquals(userMessageLog.getReceived(), messageLogRO.getReceived());
        Assert.assertEquals(userMessageLog.getSendAttempts(), messageLogRO.getSendAttempts());
    }

    @Test
    public void testSignalMessageLog() {
        // Given
        SignalMessageLog signalMessageLog = (SignalMessageLog) createMessageLog(false);
        final MessageLogInfo messageLogInfo = new MessageLogInfo(
                signalMessageLog.getMessageId(),
                signalMessageLog.getMessageStatus(),
                signalMessageLog.getNotificationStatus(),
                signalMessageLog.getMshRole(),
                signalMessageLog.getMessageType(),
                signalMessageLog.getDeleted(),
                signalMessageLog.getReceived(),
                signalMessageLog.getSendAttempts(),
                signalMessageLog.getSendAttemptsMax(),
                signalMessageLog.getNextAttempt(),
                "",
                "fromPartyId",
                "toPartyId",
                "originalSender",
                "finalRecipient",
                "refToMessageId",
                signalMessageLog.getFailed(),
                signalMessageLog.getRestored());
        final ArrayList<MessageLogInfo> resultList = new ArrayList<>();
        resultList.add(messageLogInfo);
        new Expectations() {{
           signalMessageLogDao.findAllInfoPaged(anyInt, anyInt, anyString, anyBoolean, (HashMap<String, Object>) any);
           result = resultList;
        }};

        // When
        final MessageLogResultRO messageLogResultRO = getMessageLog(MessageType.SIGNAL_MESSAGE);

        // Then
        Assert.assertNotNull(messageLogResultRO);
        Assert.assertEquals(1, messageLogResultRO.getMessageLogEntries().size());
        MessageLogRO messageLogRO = messageLogResultRO.getMessageLogEntries().get(0);

        Assert.assertEquals(signalMessageLog.getMessageId(), messageLogRO.getMessageId());
        Assert.assertEquals(signalMessageLog.getMessageStatus(), messageLogRO.getMessageStatus());
        Assert.assertEquals(signalMessageLog.getMessageType(), messageLogRO.getMessageType());
        Assert.assertEquals(signalMessageLog.getDeleted(), messageLogRO.getDeleted());
        Assert.assertEquals(signalMessageLog.getMshRole(), messageLogRO.getMshRole());
        Assert.assertEquals(signalMessageLog.getNextAttempt(), messageLogRO.getNextAttempt());
        Assert.assertEquals(signalMessageLog.getNotificationStatus(), messageLogRO.getNotificationStatus());
        Assert.assertEquals(signalMessageLog.getReceived(), messageLogRO.getReceived());
        Assert.assertEquals(signalMessageLog.getSendAttempts(), messageLogRO.getSendAttempts());
    }

    private MessageLogResultRO getMessageLog(MessageType messageType) {
        return messageLogResource.getMessageLog(1, 10, 10, "MessageId", true,
                null, null, null, messageType, null,
                null, null, null, null, null, null,
                null, null);
    }
}
