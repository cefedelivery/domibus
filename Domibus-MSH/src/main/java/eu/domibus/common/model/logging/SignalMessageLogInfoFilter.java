package eu.domibus.common.model.logging;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;

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
                "SignalMessage message " +
                "left join log.messageInfo info " +
                "left join message.messageProperties.property propsFrom " +
                "left join message.messageProperties.property propsTo " +
                "left join message.partyInfo.from.partyId partyFrom " +
                "left join message.partyInfo.to.partyId partyTo " +
                "where message.messageInfo = info and propsFrom.name = 'originalSender'" +
                "and propsTo.name = 'finalRecipient'";
        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }
}
