package eu.domibus.common.model.logging;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@Service(value = "signalMessageLogInfoFilter")
public class SignalMessageLogInfoFilter extends MessageLogInfoFilter {

    @Override
    protected String getHQLKey(String originalColumn) {
        if(StringUtils.equals(originalColumn, "conversationId")) {
            return "";
        } else {
            return super.getHQLKey(originalColumn);
        }
    }

    @Override
    protected StringBuilder filterQuery(String query, String column, boolean asc, HashMap<String, Object> filters) {
        if(StringUtils.isNotEmpty(String.valueOf(filters.get("conversationId")))) {
            filters.put("conversationId",null);
        }
        return super.filterQuery(query,column,asc,filters);
    }

    public String filterSignalMessageLogQuery(String column, boolean asc, HashMap<String, Object> filters) {
        String query = "select new eu.domibus.common.model.logging.MessageLogInfo(log, partyFrom.value, partyTo.value, propsFrom.value, propsTo.value, info.refToMessageId) from SignalMessageLog log, " +
                "Messaging messaging inner join messaging.signalMessage signal " +
                "inner join messaging.userMessage message " +
                "left join message.messageInfo info " +
                "left join message.messageProperties.property propsFrom " +
                "left join message.messageProperties.property propsTo " +
                "left join message.partyInfo.from.partyId partyFrom " +
                "left join message.partyInfo.to.partyId partyTo " +
                "where signal.messageInfo.messageId=log.messageId and  signal.messageInfo.refToMessageId=message.messageInfo.messageId and propsFrom.name = 'originalSender'" +
                "and propsTo.name = 'finalRecipient' " ;
        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }

    public String countSignalMessageLogQuery(boolean asc, HashMap<String, Object> filters) {
        String query = "select count(message.id) from SignalMessageLog log, " +
                "Messaging messaging inner join messaging.signalMessage signal " +
                "inner join messaging.userMessage message " +
                "left join message.messageInfo info " +
                "left join message.messageProperties.property propsFrom " +
                "left join message.messageProperties.property propsTo " +
                "left join message.partyInfo.from.partyId partyFrom " +
                "left join message.partyInfo.to.partyId partyTo " +
                "where signal.messageInfo.messageId=log.messageId and signal.messageInfo.refToMessageId=message.messageInfo.messageId and propsFrom.name = 'originalSender'" +
                "and propsTo.name = 'finalRecipient' ";
        StringBuilder result = filterQuery(query, null, asc, filters);
        return result.toString();
    }
}
