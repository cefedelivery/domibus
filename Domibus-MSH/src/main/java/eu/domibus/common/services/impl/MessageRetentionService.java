package eu.domibus.common.services.impl;

import java.util.List;

/**
 * Responsible for the retention and clean up of Domibus messages, including signal messages. *
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface MessageRetentionService {


    /**
     * Deletes the expired messages(downloaded or not) using the configured limits
     */
    void deleteExpiredMessages();

    void deleteExpiredMessages(String mpc, Integer expiredDownloadedMessagesLimit, Integer expiredNotDownloadedMessagesLimit);

    void scheduleDeleteMessages(List<String> messageIds);
}
