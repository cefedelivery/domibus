package eu.domibus.core.replication;

import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.web.rest.ro.MessageLogResultRO;

import java.util.List;
import java.util.Map;

/**
 * Service class for handling business for {@link UIMessageEntity}
 *
 * @author Catalin Enache
 * @since 4.0
 */
public interface UIMessageService {

    List<MessageLogInfo> findPaged(int from, int max, String column, boolean asc, Map<String, Object> filters);

    MessageLogResultRO countAndFindPaged(int from, int max, String column, boolean asc, Map<String, Object> filters);

    void saveOrUpdate(UIMessageEntity uiMessageEntity);
}
