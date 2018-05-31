package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessagingLock;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
public interface MessagingLockDao {

    PullMessageId getNextPullMessageToProcess(Integer messageId);

    /*void releaseLock(MessagingLock messagingLock);*/

    MessagingLock getLock(String messageId);

    void save(MessagingLock messagingLock);

    void delete(String messageId);

    void delete(MessagingLock messagingLock);

    MessagingLock findMessagingLockForMessageId(String messageId);

    List<MessagingLock> findStaledMessages();


    List<MessagingLock> findDeletedMessages();

    List<MessagingLock> findReadyToPull(String mpc, String initiator);

    List<MessagingLock> findWaitingForReceipt();

    MessagingLock getMessagingLock(Integer id);
}
