package eu.domibus.common.model.logging;

import mockit.Expectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class UserMessageLogInfoFilterTest {

    public static final String QUERY = "select new eu.domibus.common.model.logging.MessageLogInfo(log, message.collaborationInfo.conversationId, partyFrom.value, partyTo.value, propsFrom.value, propsTo.value, info.refToMessageId) from UserMessageLog log, " +
            "UserMessage message " +
            "left join log.messageInfo info " +
            "left join message.messageProperties.property propsFrom " +
            "left join message.messageProperties.property propsTo " +
            "left join message.partyInfo.from.partyId partyFrom " +
            "left join message.partyInfo.to.partyId partyTo " +
            "where message.messageInfo = info and propsFrom.name = 'originalSender'" +
            "and propsTo.name = 'finalRecipient'";

    @Tested
    UserMessageLogInfoFilter userMessageLogInfoFilter;

    private static HashMap<String, Object> filters = new HashMap<>();

    @BeforeClass
    public static void before() {
        filters = MessageLogInfoFilterTest.returnFilters();
    }

    @Test
    public void createUserMessageLogInfoFilter() {
        new Expectations(userMessageLogInfoFilter) {{
            userMessageLogInfoFilter.filterQuery(anyString,anyString,anyBoolean,filters);
            result = QUERY;
        }};

        String query = userMessageLogInfoFilter.filterUserMessageLogQuery("column", true, filters);

        Assert.assertEquals(QUERY, query);
    }

    @Test
    public void testGetHQLKeyConversationId() {
        Assert.assertEquals("message.collaborationInfo.conversationId", userMessageLogInfoFilter.getHQLKey("conversationId"));
    }

    @Test
    public void testGetHQLKeyMessageId() {
        Assert.assertEquals("log.messageStatus", userMessageLogInfoFilter.getHQLKey("messageStatus"));
    }

    @Test
    public void testFilterQuery() {
        StringBuilder resultQuery = userMessageLogInfoFilter.filterQuery("select * from table where column = ''","messageId", true, filters);
        String resultQueryString = resultQuery.toString();
        Assert.assertTrue(resultQueryString.contains("log.notificationStatus = :notificationStatus"));
        Assert.assertTrue(resultQueryString.contains("partyFrom.value = :fromPartyId"));
        Assert.assertTrue(resultQueryString.contains("log.sendAttemptsMax = :sendAttemptsMax"));
        Assert.assertTrue(resultQueryString.contains("propsFrom.value = :originalSender"));
        Assert.assertTrue(resultQueryString.contains("log.received <= :receivedTo"));
        Assert.assertTrue(resultQueryString.contains("log.messageId = :messageId"));
        Assert.assertTrue(resultQueryString.contains("info.refToMessageId = :refToMessageId"));
        Assert.assertTrue(resultQueryString.contains("log.received = :received"));
        Assert.assertTrue(resultQueryString.contains("log.sendAttempts = :sendAttempts"));
        Assert.assertTrue(resultQueryString.contains("propsTo.value = :finalRecipient"));
        Assert.assertTrue(resultQueryString.contains("log.nextAttempt = :nextAttempt"));
        Assert.assertTrue(resultQueryString.contains("log.messageStatus = :messageStatus"));
        Assert.assertTrue(resultQueryString.contains("log.deleted = :deleted"));
        Assert.assertTrue(resultQueryString.contains("log.messageType = :messageType"));
        Assert.assertTrue(resultQueryString.contains("log.received >= :receivedFrom"));
        Assert.assertTrue(resultQueryString.contains("partyTo.value = :toPartyId"));
        Assert.assertTrue(resultQueryString.contains("log.mshRole = :mshRole"));
        Assert.assertTrue(resultQueryString.contains("order by log.messageId asc"));
    }


}
