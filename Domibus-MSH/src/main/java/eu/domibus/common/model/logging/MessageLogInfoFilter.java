package eu.domibus.common.model.logging;

import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageLogInfoFilter {

    private static final String LOG_MESSAGE_ID = "log.messageId";
    private static final String LOG_MSH_ROLE = "log.mshRole";
    private static final String LOG_MESSAGE_TYPE = "log.messageType";
    private static final String LOG_MESSAGE_STATUS = "log.messageStatus";
    private static final String LOG_NOTIFICATION_STATUS = "log.notificationStatus";
    private static final String LOG_DELETED = "log.deleted";
    private static final String LOG_RECEIVED = "log.received";
    private static final String LOG_SEND_ATTEMPTS = "log.sendAttempts";
    private static final String LOG_SEND_ATTEMPTS_MAX = "log.sendAttemptsMax";
    private static final String LOG_NEXT_ATTEMPT = "log.nextAttempt";
    private static final String PARTY_FROM_VALUE = "partyFrom.value";
    private static final String PARTY_TO_VALUE = "partyTo.value";
    private static final String INFO_REF_TO_MESSAGE_ID = "info.refToMessageId";
    private static final String PROPS_FROM_VALUE = "propsFrom.value";
    private static final String PROPS_TO_VALUE = "propsTo.value";
    private static final String MESSAGE_COLLABORATION_INFO_CONVERSATION_ID = "message.collaborationInfo.conversationId";
    private static final String LOG_FAILED = "log.failed";
    private static final String LOG_RESTORED = "log.restored";

    protected String getHQLKey(String originalColumn) {
        switch(originalColumn) {
            case "messageId":
                return LOG_MESSAGE_ID;
            case "mshRole":
                return LOG_MSH_ROLE;
            case "messageType":
                return LOG_MESSAGE_TYPE;
            case "messageStatus":
                return LOG_MESSAGE_STATUS;
            case "notificationStatus":
                return LOG_NOTIFICATION_STATUS;
            case "deleted":
                return LOG_DELETED;
            case "received":
                return LOG_RECEIVED;
            case "sendAttempts":
                return LOG_SEND_ATTEMPTS;
            case "sendAttemptsMax":
                return LOG_SEND_ATTEMPTS_MAX;
            case "nextAttempt":
                return LOG_NEXT_ATTEMPT;
            case "fromPartyId":
                return PARTY_FROM_VALUE;
            case "toPartyId":
                return PARTY_TO_VALUE;
            case "refToMessageId":
                return INFO_REF_TO_MESSAGE_ID;
            case "originalSender":
                return PROPS_FROM_VALUE;
            case "finalRecipient":
                return PROPS_TO_VALUE;
            case "conversationId":
                return MESSAGE_COLLABORATION_INFO_CONVERSATION_ID;
            case "failed":
                return LOG_FAILED;
            case "restored":
                return LOG_RESTORED;
            default:
                return "";
        }
    }

    protected StringBuilder filterQuery(String query, String column, boolean asc, Map<String, Object> filters) {
        StringBuilder result = new StringBuilder(query);
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            handleFilter(result, filter);
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

    private void handleFilter(StringBuilder result, Map.Entry<String, Object> filter) {
        if (filter.getValue() != null) {
            result.append(" and ");
            if (!(filter.getValue() instanceof Date)) {
                if (!(filter.getValue().toString().isEmpty())) {
                    String tableName = getHQLKey(filter.getKey());
                    result.append(tableName).append(" = :").append(filter.getKey());
                }
            } else {
                if (!(filter.getValue().toString().isEmpty())) {
                    String s = filter.getKey();
                    if (s.equals("receivedFrom")) {
                        result.append(LOG_RECEIVED).append(" >= :").append(filter.getKey());
                    } else if (s.equals("receivedTo")) {
                        result.append(LOG_RECEIVED).append(" <= :").append(filter.getKey());
                    }
                }
            }
        }
    }

    public <E> TypedQuery<E> applyParameters(TypedQuery<E> query, Map<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                query.setParameter(filter.getKey(), filter.getValue());
            }
        }
        return query;
    }
}
