package eu.domibus.common.model.logging;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@Service(value = "signalMessageLogInfoFilter")
public class SignalMessageLogInfoFilter extends MessageLogInfoFilter {

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
                (isFourCornerModel() ? " propsFrom.value," : "'',") +
                (isFourCornerModel() ? " propsTo.value," : "'',") +
                " info.refToMessageId," +
                "log.failed," +
                "log.restored," +
                "log.messageSubtype" +
                ")" + getQueryBody(filters);
        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }

    public String countSignalMessageLogQuery(boolean asc, Map<String, Object> filters) {
        String query = "select count(message.id)" + getQueryBody(filters);
        StringBuilder result = filterQuery(query, null, asc, filters);
        return result.toString();
    }

    private String getQueryBody(Map<String, Object> filters) {
        return
                " from SignalMessageLog log, " +
                        "Messaging messaging inner join messaging.signalMessage signal " +
                        "inner join messaging.userMessage message " +
                        "left join signal.messageInfo info " +
                        (isFourCornerModel() ? "left join message.messageProperties.property propsFrom " +
                         "left join message.messageProperties.property propsTo " : StringUtils.EMPTY) +
                        "left join message.partyInfo.from.partyId partyFrom " +
                        "left join message.partyInfo.to.partyId partyTo " +
                        "where signal.messageInfo.messageId=log.messageId and signal.messageInfo.refToMessageId=message.messageInfo.messageId " +
                        (isFourCornerModel() ? "and propsFrom.name = 'originalSender' and propsTo.name = 'finalRecipient' " : StringUtils.EMPTY);
    }
}
