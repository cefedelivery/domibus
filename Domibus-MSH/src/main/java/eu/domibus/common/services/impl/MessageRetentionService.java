package eu.domibus.common.services.impl;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface MessageRetentionService {

    void deleteExpiredMessages();

    void deleteExpiredMessages(String mpc, Integer expiredDownloadedMessagesLimit, Integer expiredNotDownloadedMessagesLimit);

    void scheduleDeleteMessages(List<String> messageIds);
}
