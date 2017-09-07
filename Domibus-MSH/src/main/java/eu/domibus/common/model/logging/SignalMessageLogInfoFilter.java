package eu.domibus.common.model.logging;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@Service(value = "signalMessageLogInfoFilter")
public class SignalMessageLogInfoFilter extends MessageLogInfoFilter {

    private static final String QUERY_BODY = " from SignalMessageLog log, " +
            "Messaging messaging inner join messaging.signalMessage signal " +
            "inner join messaging.userMessage message " +
            "left join message.messageInfo info " +
            "left join message.messageProperties.property propsFrom " +
            "left join message.messageProperties.property propsTo " +
            "left join message.partyInfo.from.partyId partyFrom " +
            "left join message.partyInfo.to.partyId partyTo " +
            "where signal.messageInfo.messageId=log.messageId and signal.messageInfo.refToMessageId=message.messageInfo.messageId and propsFrom.name = 'originalSender'" +
            "and propsTo.name = 'finalRecipient' ";
    private static final String CONVERSATION_ID = "conversationId";

    @Override
    protected String getHQLKey(String originalColumn) {
        if(StringUtils.equals(originalColumn, CONVERSATION_ID)) {
            return "";
        } else {
            return super.getHQLKey(originalColumn);
        }
    }

    @Override
    protected StringBuilder filterQuery(String query, String column, boolean asc, Map<String, Object> filters) {
        if(StringUtils.isNotEmpty(String.valueOf(filters.get(CONVERSATION_ID)))) {
            filters.put(CONVERSATION_ID,null);
        }
        return super.filterQuery(query,column,asc,filters);
    }

    public String filterSignalMessageLogQuery(String column, boolean asc, Map<String, Object> filters) {
        String query = "select new eu.domibus.common.model.logging.MessageLogInfo(" +
                "log.messageId," +
                "log.messageStatus," +
                "log.notificationStatus," +
                "log.mshRole," +
                "log.messageType," +
                "log.deleted," +
                "log.received," +
                "log.sendAttempts," +
                "log.sendAttemptsMax," +
                "log.nextAttempt," +
                "''," +
                " partyFrom.value," +
                " partyTo.value," +
                " propsFrom.value," +
                " propsTo.value," +
                " info.refToMessageId," +
                "log.failed," +
                "log.restored" +
                ")" + QUERY_BODY;
        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }

    public String countSignalMessageLogQuery(boolean asc, Map<String, Object> filters) {
        String query = "select count(message.id)" + QUERY_BODY;
        StringBuilder result = filterQuery(query, null, asc, filters);
        return result.toString();
    }
}
