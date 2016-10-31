package eu.domibus.common.services;

import eu.domibus.common.model.logging.MessageLog;

import java.util.HashMap;
import java.util.List;

/**
 * @author Federico Martini
 * @since 3.2
 */
public interface MessagesLogService {

    List<? extends MessageLog> findMessageLogs(int page, int size, String column, boolean asc, HashMap<String, Object> filters);

    Long countMessages(int size, HashMap<String, Object> filters);

}
