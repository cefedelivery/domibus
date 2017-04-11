package eu.domibus.common.model.logging;

import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageLogInfoFilter {

    protected String getHQLKey(String originalColumn) {
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

    protected StringBuilder filterQuery(String query, String column, boolean asc, HashMap<String, Object> filters) {
        StringBuilder result = new StringBuilder(query);
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                result.append(" and ");
                if (!(filter.getValue() instanceof Date)) {
                    if (!(filter.getValue().toString().isEmpty())) {
                        String tableName = getHQLKey(filter.getKey());
                        result.append(tableName).append(" = :").append(filter.getKey());
                    }
                } else {
                    if (!(filter.getValue().toString().isEmpty())) {
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
            String usedColumn = getHQLKey(column);
            if (asc) {
                result.append(" order by ").append(usedColumn).append(" asc");
            } else {
                result.append(" order by ").append(usedColumn).append(" desc");
            }
        }

        return result;
    }

    public TypedQuery<MessageLogInfo> applyParameters(TypedQuery<MessageLogInfo> query, HashMap<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                query.setParameter(filter.getKey(), filter.getValue());
            }
        }
        return query;
    }
}
