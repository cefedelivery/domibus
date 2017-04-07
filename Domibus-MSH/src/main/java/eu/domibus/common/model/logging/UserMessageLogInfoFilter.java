package eu.domibus.common.model.logging;

import org.springframework.stereotype.Service;

import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@Service(value = "userMessageLogInfoFilter")
public class UserMessageLogInfoFilter {

    private String returnCorrectColumn(String originalColumn) {
        switch(originalColumn) {
            case "messageId":
                return "log.messageId";
            case "mshRole":
                return "log.mshRole";
            case "messageType":
                return "log.messageType";
            case "messageStatus":
                return "log.messageStatus";
            case "notificationStatus":
                return "log.notificationStatus";
            case "deleted":
                return "log.deleted";
            case "received":
                return "log.received";
            case "sendAttempts":
                return "log.sendAttempts";
            case "sendAttemptsMax":
                return "log.sendAttemptsMax";
            case "nextAttempt":
                return "log.nextAttempt";
            case "fromPartyId":
                return "partyFrom.value";
            case "toPartyId":
                return "partyTo.value";
            case "refToMessageId":
                return "info.refToMessageId";
            case "originalSender":
                return "propsFrom.value";
            case "finalRecipient":
                return "propsTo.value";
            case "conversationId":
                return "message.collaborationInfo.conversationId";
            default:
                return "";
        }
    }

    public String filterUserMessageLogQuery(String column, boolean asc, HashMap<String, Object> filters) {
        String query = "select new eu.domibus.common.model.logging.UserMessageLogInfo(log, message.collaborationInfo.conversationId, partyFrom.value, partyTo.value, propsFrom.value, propsTo.value, info.refToMessageId) from UserMessageLog log, " +
                "UserMessage message " +
                "left join log.messageInfo info " +
                "left join message.messageProperties.property propsFrom " +
                "left join message.messageProperties.property propsTo " +
                "left join message.partyInfo.from.partyId partyFrom " +
                "left join message.partyInfo.to.partyId partyTo " +
                "where message.messageInfo = info and propsFrom.name = 'originalSender'" +
                "and propsTo.name = 'finalRecipient'";

        StringBuilder result = new StringBuilder(query);
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                result.append(" and ");
                if (!(filter.getValue() instanceof Date)) {
                    if (!filter.getValue().toString().isEmpty()) {
                        String tableName = returnCorrectColumn(filter.getKey());
                        result.append(tableName).append(" = :").append(filter.getKey());
                    }
                } else {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey()) {
                            case "":
                                break;
                            case "receivedFrom":
                                result.append("log.received").append(" >= :").append(filter.getKey());
                                break;
                            case "receivedTo":
                                result.append("log.received").append(" <= :").append(filter.getKey());
                                break;
                        }
                    }
                }
            }
        }

        if (column != null) {
            String usedColumn = returnCorrectColumn(column);
            if (asc) {
                result.append(" order by ").append(usedColumn).append(" asc");
            } else {
                result.append(" order by ").append(usedColumn).append(" desc");
            }
        }
        return result.toString();
    }

    public TypedQuery<UserMessageLogInfo> applyParameters(TypedQuery<UserMessageLogInfo> query, HashMap<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                query.setParameter(filter.getKey(), filter.getValue());
            }
        }
        return query;
    }
}
