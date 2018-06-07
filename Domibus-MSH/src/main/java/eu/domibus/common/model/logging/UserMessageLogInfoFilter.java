package eu.domibus.common.model.logging;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@Service(value = "userMessageLogInfoFilter")
public class UserMessageLogInfoFilter extends MessageLogInfoFilter {

    public String filterUserMessageLogQuery(String column, boolean asc, Map<String, Object> filters) {
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
                "message.collaborationInfo.conversationId," +
                "partyFrom.value," +
                "partyTo.value," +
                (isFourCornerModel() ? "propsFrom.value," : "'',") +
                (isFourCornerModel() ? "propsTo.value," : "'',") +
                "info.refToMessageId," +
                "log.failed," +
                "log.restored" +
                ")" + getQueryBody(filters);
        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }

    public String countUserMessageLogQuery(boolean asc, Map<String, Object> filters) {
        String query = "select count(message.id)" + getQueryBody(filters);

        StringBuilder result = filterQuery(query, null, asc, filters);
        return result.toString();
    }

    /**
     * Constructs the query body based on different conditions
     *
     * @param filters values from GUI
     * @return String query body
     */
    private String getQueryBody(Map<String, Object> filters) {
        return
                " from UserMessageLog log, " +
                        "UserMessage message " +
                        "left join log.messageInfo info " +
                        (isFourCornerModel() ?
                                "left join message.messageProperties.property propsFrom "  +
                                "left join message.messageProperties.property propsTo " : StringUtils.EMPTY) +
                        "left join message.partyInfo.from.partyId partyFrom " +
                        "left join message.partyInfo.to.partyId partyTo " +
                        "where message.messageInfo = info " +
                        (isFourCornerModel() ?
                                "and propsFrom.name = 'originalSender' "  +
                                "and propsTo.name = 'finalRecipient' " : StringUtils.EMPTY);

    }



}
