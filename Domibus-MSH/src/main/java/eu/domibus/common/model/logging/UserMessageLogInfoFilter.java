package eu.domibus.common.model.logging;

import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@Service(value = "userMessageLogInfoFilter")
public class UserMessageLogInfoFilter extends MessageLogInfoFilter {

    public String filterUserMessageLogQuery(String column, boolean asc, HashMap<String, Object> filters) {
        String query = "select new eu.domibus.common.model.logging.MessageLogInfo(log.messageId,log.messageStatus,log.notificationStatus,log.mshRole , log.messageType, log.deleted,log.received,log.sendAttempts, log.sendAttemptsMax,log.nextAttempt,message.collaborationInfo.conversationId, partyFrom.value, partyTo.value, propsFrom.value, propsTo.value, info.refToMessageId) from UserMessageLog log, " +
                "UserMessage message " +
                "left join log.messageInfo info " +
                "left join fetch message.messageProperties.property propsFrom " +
                "left join fetch message.messageProperties.property propsTo " +
                "left join fetch message.partyInfo.from.partyId partyFrom " +
                "left join fetch message.partyInfo.to.partyId partyTo " +
                "where message.messageInfo = info and propsFrom.name = 'originalSender'" +
                "and propsTo.name = 'finalRecipient'";

        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }

    public String countUserMessageLogQuery(boolean asc, HashMap<String, Object> filters) {
        String query = "select count(message.id) from UserMessageLog log, " +
                "UserMessage message " +
                "left join log.messageInfo info " +
                "left join message.messageProperties.property propsFrom " +
                "left join message.messageProperties.property propsTo " +
                "left join message.partyInfo.from.partyId partyFrom " +
                "left join message.partyInfo.to.partyId partyTo " +
                "where message.messageInfo = info and propsFrom.name = 'originalSender'" +
                "and propsTo.name = 'finalRecipient'";

        StringBuilder result = filterQuery(query, null, asc, filters);
        return result.toString();
    }
}
