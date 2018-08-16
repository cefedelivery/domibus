package eu.domibus.common.services;

import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.web.rest.ro.MessageLogResultRO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Federico Martini
 * @since 3.2
 */
public interface MessagesLogService {

    List<? extends MessageLog> findMessageLogs(int page, int size, String column, boolean asc, HashMap<String, Object> filters);

    Long countMessages(int size, HashMap<String, Object> filters);

    MessageLogResultRO countAndFindPaged(MessageType messageType, int from, int max, String orderByColumn, boolean asc, Map<String, Object> filters);

    List<MessageLogInfo> findAllInfoCSV(MessageType messageType, int max, String orderByColumn, boolean asc, Map<String, Object> filters);

}
