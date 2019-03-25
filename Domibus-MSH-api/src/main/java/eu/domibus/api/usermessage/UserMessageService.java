package eu.domibus.api.usermessage;

import eu.domibus.api.usermessage.domain.UserMessage;

import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageService {

    String COMMAND_SOURCE_MESSAGE_REJOIN = "SourceMessageRejoin";
    String COMMAND_SOURCE_MESSAGE_RECEIPT = "SourceMessageReceipt";
    String COMMAND_SOURCE_MESSAGE_REJOIN_FILE = "SourceMessageRejoinFile";
    String COMMAND_SPLIT_AND_JOIN_SEND_FAILED = "SplitAndJoinSendFailed";
    String COMMAND_USER_MESSAGE_FRAGMENT_FAILED = "UserMessageFragmentFailed";

    String MSG_SOURCE_MESSAGE_FILE = "SourceMessageFile";
    String MSG_TYPE = "messageType";
    String MSG_GROUP_ID = "groupId";
    String MSG_BACKEND_NAME = "backendName";
    String MSG_SOURCE_MESSAGE_ID = "sourceMessageId";
    String MSG_USER_MESSAGE_ID = "userMessageId";

    String PULL_RECEIPT_REF_TO_MESSAGE_ID = "pullReceiptRefToMessageId";

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

    void scheduleSplitAndJoinGroupFailed(String groupId, String backendName);

    void scheduleSourceMessageSending(String messageId);

    void scheduleUserMessageFragmentFailed(String messageId);

    void scheduleSourceMessageRejoinFile(String groupId, String backendName);

    void scheduleSourceMessageRejoin(String groupId, String file, String backendName);

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
