package eu.domibus.core.pull;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
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
     * @param userMessage
     * @param messageLog
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
     *
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
     * Manage the status of the pull message when the receipt arrives..
     *
     * @param reliabilityCheckSuccessful the state of the reality chek process.
     * @param isOk
     * @param userMessageLog             the message log.
     * @param legConfiguration           contains the context of the configured message exchange.
     */
    void updatePullMessageAfterReceipt(
            ReliabilityChecker.CheckResult reliabilityCheckSuccessful,
            ResponseHandler.CheckResult isOk,
            UserMessageLog userMessageLog,
            LegConfiguration legConfiguration
    );

    /**
     * Retrieve waiting for receipt message for which next attempt date is passed.
     * Either put them back in ready to pull or send failure.
     */
    void resetWaitingForReceiptPullMessages();
}
