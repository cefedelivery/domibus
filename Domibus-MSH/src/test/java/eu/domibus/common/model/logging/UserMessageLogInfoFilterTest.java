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
    public static final String EXPECTED_QUERY = "select * from table where column = '' and log.notificationStatus = :notificationStatus and partyFrom.value = :fromPartyId and log.sendAttemptsMax = :sendAttemptsMax and propsFrom.value = :originalSender and log.received <= :receivedTo and message.collaborationInfo.conversationId = :conversationId and log.messageId = :messageId and info.refToMessageId = :refToMessageId and log.received = :received and log.sendAttempts = :sendAttempts and propsTo.value = :finalRecipient and log.nextAttempt = :nextAttempt and log.messageStatus = :messageStatus and log.deleted = :deleted and log.messageType = :messageType and log.received >= :receivedFrom and partyTo.value = :toPartyId and log.mshRole = :mshRole order by log.messageId asc";
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
        Assert.assertEquals(EXPECTED_QUERY, resultQueryString);
    }


}
