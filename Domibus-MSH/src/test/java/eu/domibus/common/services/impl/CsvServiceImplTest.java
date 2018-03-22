package eu.domibus.common.services.impl;

import eu.domibus.api.csv.CsvException;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.ebms3.common.model.MessageSubtype;
import eu.domibus.ebms3.common.model.MessageType;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@RunWith(JMockit.class)
public class CsvServiceImplTest {

    @Tested
    CsvServiceImpl csvServiceImpl;

    @Test
    public void testExportToCsv_EmptyList() throws CsvException {
        // Given
        // When
        final String exportToCSV = csvServiceImpl.exportToCSV(new ArrayList<>());

        // Then
        Assert.assertTrue(exportToCSV.isEmpty());
    }

    @Test
    public void testExportToCsv_NullList() throws CsvException {
        // Given
        // When
        final String exportToCSV = csvServiceImpl.exportToCSV(null);

        // Then
        Assert.assertTrue(exportToCSV.isEmpty());
    }

    @Test
    public void testExportToCsv() throws CsvException {
        testExportCsvBySubtype(null);
    }

    @Test
    public void testExportToCsvTest() throws CsvException {
        testExportCsvBySubtype(MessageSubtype.TEST);
    }

    private void testExportCsvBySubtype(MessageSubtype messageSubtype) {
        // Given
        Date date = new Date();
        List<MessageLogInfo> messageLogInfoList = getMessageList(MessageType.USER_MESSAGE, date, messageSubtype);

        // When
        final String exportToCSV = csvServiceImpl.exportToCSV(messageLogInfoList);

        // Then
        Assert.assertTrue(exportToCSV.contains("Message Id,From Party Id,To Party Id,Message Status,Notification Status,Received,Msh Role,Send Attempts,Send Attempts Max,Next Attempt,Conversation Id,Message Type,Message Subtype,Deleted,Original Sender,Final Recipient,Ref To Message Id,Failed,Restored"));
        Assert.assertTrue(exportToCSV.contains("messageId,fromPartyId,toPartyId,ACKNOWLEDGED,NOTIFIED,"+date+",RECEIVING,1,5,"+date+",conversationId,USER_MESSAGE,"+(messageSubtype!=null?messageSubtype.name():"")+","+date+",originalSender,finalRecipient,refToMessageId,"+date+","+date));
    }

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
