package eu.domibus.core.pull;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;

public interface PullMessageService {


    /**
     * Search if a message ready for being pull exists for that initiator/mpc combination.
     * If it exists the message will be locked until the end of the transaction.
     *
     * @param initiator the party initiating the pull request.
     * @param mpc       the mpc contained in the pull request.
     * @return the id of the message or null.
     */
    String getPullMessageId(String initiator, String mpc);

    /**
     * When a message arrives in the system, if it is configured to be pulled, some extra information needed for finding
     * the message later will be extracted and saved in a different place where the message lock will be facilitated.
     *
     * @param partyIdExtractor interface allowing to retrieve the initiator information from different context.
     * @param userMessage      the user message.
     * @param messageLog       the message log.
     */
    void addPullMessageLock(PartyIdExtractor partyIdExtractor, UserMessage userMessage, MessageLog messageLog);

    /**
     * When a message has been successfully delivered or marked a failed, its lock counter part item should be removed from
     * the  locking system.
     *
     * @param messageId the id of the message to be deleted;
     */
    void deletePullMessageLock(String messageId);

    /**
     * Manage the status of the pull message after the pull request has occured.
     * It handles happyflow and failure.
     * @param userMessage      the userMessage that has been pulled.
     * @param messageId        the id of the message.
     * @param legConfiguration contains the context of the configured message exchange.
     * @param state            the state of the pull tentative.
     */
    void updatePullMessageAfterRequest(final UserMessage userMessage,
                                       final String messageId,
                                       final LegConfiguration legConfiguration,
                                       final ReliabilityChecker.CheckResult state);

    /**
     * Manage the status of the pull message when the receipt arrives.
     * @param reliabilityCheckSuccessful the state of the reality check process.
     * @param isOk                       the OK or WARNING state of the acknowledgement.
     * @param userMessageLog             the message log.
     * @param legConfiguration           contains the context of the configured message exchange.
     * @param userMessage                not used.
     * @return the PullRequestResult of the userMessageLog
     */
    PullRequestResult updatePullMessageAfterReceipt(
            ReliabilityChecker.CheckResult reliabilityCheckSuccessful,
            ResponseHandler.CheckResult isOk,
            UserMessageLog userMessageLog,
            LegConfiguration legConfiguration,
            UserMessage userMessage);

    /**
     * Acquire a lock given a messageId.
     * @param messageId the message id.
     * @return the lock entity.
     */
    MessagingLock getLock(String messageId);

    /**
     * Delete a lock in a new transaction..
     *
     * @param messageId the id of the lock to delete.
     */
    void deleteInNewTransaction(String messageId);

    /**
     * Delete a lock.
     * @param messagingLock the entity to delete.
     */
    void delete(MessagingLock messagingLock);

    /**
     * Given a message id, set the message in waiting for receipt state into ready to pull.
     * @param messageId the message id.
     */
    void resetMessageInWaitingForReceiptState(String messageId);

    /**
     * Handle message expiration when expiration date is reached.
     * @param messageId the message id.
     */
    void expireMessage(String messageId);

    /**
     * Handles the lock in regards of message receipt status.
     * @param requestResult the pull request result.
     */
    void releaseLockAfterReceipt(PullRequestResult requestResult);
}
