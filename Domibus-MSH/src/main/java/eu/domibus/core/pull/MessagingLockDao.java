package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessagingLock;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
public interface MessagingLockDao {

    PullMessageId getNextPullMessageToProcess(Long messageId);

    void save(MessagingLock messagingLock);

    void delete(String messageId);

    MessagingLock findMessagingLockForMessageId(String messageId);
}
