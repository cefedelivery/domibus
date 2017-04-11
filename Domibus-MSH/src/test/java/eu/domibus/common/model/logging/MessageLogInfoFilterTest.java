package eu.domibus.common.model.logging;

import mockit.Expectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.HashMap;

import static org.mockito.Mockito.spy;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageLogInfoFilterTest {

    public static final String QUERY1 = "select * from table where z = 1 and log.notificationStatus = :notificationStatus and partyFrom.value = :fromPartyId and log.sendAttemptsMax = :sendAttemptsMax and propsFrom.value = :originalSender and log.received <= :receivedTo and message.collaborationInfo.conversationId = :conversationId and log.messageId = :messageId and info.refToMessageId = :refToMessageId and log.received = :received and log.sendAttempts = :sendAttempts and propsTo.value = :finalRecipient and log.nextAttempt = :nextAttempt and log.messageStatus = :messageStatus and log.deleted = :deleted and log.messageType = :messageType and log.received >= :receivedFrom and partyTo.value = :toPartyId and log.mshRole = :mshRole order by log.messageStatus";
    @Tested
    MessageLogInfoFilter messageLogInfoFilter;

    public static HashMap<String, Object> returnFilters() {
        HashMap<String, Object> filters = new HashMap<>();

        filters.put("conversationId", "CONVERSATIONID");
        filters.put("messageId", "MESSAGEID");
        filters.put("mshRole", "MSHROLE");
        filters.put("messageType", "MESSAGETYPE");
        filters.put("messageStatus", "MESSAGESTATUS");
        filters.put("notificationStatus", "NOTIFICATIONSTATUS");
        filters.put("deleted", "DELETED");
        filters.put("received", "RECEIVED");
        filters.put("sendAttempts", "SENDATTEMPTS");
        filters.put("sendAttemptsMax", "SENDATTEMPTSMAX");
        filters.put("nextAttempt", "NEXTATTEMPT");
        filters.put("fromPartyId", "FROMPARTYID");
        filters.put("toPartyId", "TOPARTYID");
        filters.put("refToMessageId", "REFTOMESSAGEID");
        filters.put("originalSender", "ORIGINALSENDER");
        filters.put("finalRecipient", "FINALRECIPIENT");
        filters.put("receivedFrom", new Date());
        filters.put("receivedTo", new Date());

        return filters;
    }


    @Test
    public void testGetHQLKeyMessageStatus() {
        Assert.assertEquals("log.messageStatus", messageLogInfoFilter.getHQLKey("messageStatus"));
    }

    @Test
    public void testGetHQLKeyFromPartyId() {
        Assert.assertEquals("partyFrom.value", messageLogInfoFilter.getHQLKey("fromPartyId"));
    }

    @Test
    public void testFilterQueryDesc() {
        StringBuilder filterQuery = messageLogInfoFilter.filterQuery("select * from table where z = 1", "messageStatus", false, returnFilters());

        String filterQueryString = filterQuery.toString();
        Assert.assertEquals(QUERY1 + " desc", filterQueryString);
        Assert.assertTrue(filterQueryString.contains("log.messageStatus desc"));
    }

    @Test
    public void testFilterQueryAsc() {
        StringBuilder filterQuery = messageLogInfoFilter.filterQuery("select * from table where z = 1", "messageStatus", true, returnFilters());

        String filterQueryString = filterQuery.toString();
        Assert.assertEquals(QUERY1 + " asc", filterQueryString);
        Assert.assertTrue(filterQueryString.contains("log.messageStatus asc"));
    }

    @Test
    public void testApplyParameters() {
        TypedQuery<MessageLogInfo> typedQuery = spy(TypedQuery.class);
        TypedQuery<MessageLogInfo> messageLogInfoTypedQuery = messageLogInfoFilter.applyParameters(typedQuery, returnFilters());
    }
}
