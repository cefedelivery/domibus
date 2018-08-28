package eu.domibus.core.replication;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.0
 */
public interface UIMessageDao {

    UIMessageEntity findUIMessageByMessageId(String messageId);

    int countMessages(Map<String, Object> filters);

    List<UIMessageEntity> findPaged(int from, int max, String column, boolean asc, Map<String, Object> filters);

    void saveOrUpdate(UIMessageEntity uiMessageEntity);

    boolean updateMessageStatus(final String messageId, final MessageStatus messageStatus,
                                final Date deleted, final Date nextAttempt, final Date failed, final Date lastModified);

    boolean updateNotificationStatus(final String messageId, final NotificationStatus notificationStatus,
                                      final Date lastModified);

}
