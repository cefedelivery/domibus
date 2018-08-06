package eu.domibus.core.replication;

import java.util.List;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.0
 */
public interface UIMessageDao {
    /** message id */
    String MESSAGE_ID = "MESSAGE_ID";

    UIMessageEntity findUIMessageByMessageId(String messageId);

    int countMessages(Map<String, Object> filters);

    List<UIMessageEntity> findPaged(int from, int max, String column, boolean asc, Map<String, Object> filters);

    void saveOrUpdate(UIMessageEntity uiMessageEntity);

}
