package eu.domibus.api.usermessage;

import eu.domibus.api.usermessage.domain.UserMessage;

import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageService {

    String MSG_SOURCE_MESSAGE_REJOIN = "SourceMessageRejoin";
    String MSG_SOURCE_MESSAGE_RECEIPT = "SourceMessageReceipt";
    String MSG_TYPE = "messageType";
    String MSG_GROUP_ID = "groupId";
    String MSG_SOURCE_MESSAGE_ID = "sourceMessageId";

    public static final String PULL_RECEIPT_REF_TO_MESSAGE_ID = "pullReceiptRefToMessageId";

    String getFinalRecipient(final String messageId);

    List<String> getFailedMessages(String finalRecipient);

    Long getFailedMessageElapsedTime(String messageId);

    void restoreFailedMessage(String messageId);

    void sendEnqueuedMessage(String messageId);

    List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end, String finalRecipient);

    void deleteFailedMessage(String messageId);

    void delete(List<String> messageIds);

    void deleteMessage(String messageId);

    void scheduleSending(String messageId);

    void scheduleSourceMessageSending(String messageId);

    void scheduleSourceMessageRejoin(String groupId);

    void scheduleSourceMessageReceipt(String messageId, String pmodeKey);

    void scheduleSending(String messageId, Long delay);

    void scheduleSending(String messageId, int retryCount);

    /**
     * Schedule the sending of the asynchronous Pull Receipt
     *
     * @param messageId MessageId of the UserMessage (for which the pull receipt was generated)
     * @param pmodeKey  the pmode key of the UserMessage
     */
    void scheduleSendingPullReceipt(String messageId, String pmodeKey);

    /**
     * Gets a User Message based on the {@code messageId}
     *
     * @param messageId User Message Identifier
     * @return User Message {@link UserMessage}
     */
    UserMessage getMessage(String messageId);
}
