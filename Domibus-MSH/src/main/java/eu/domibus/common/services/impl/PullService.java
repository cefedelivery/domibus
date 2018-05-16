package eu.domibus.common.services.impl;

import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.core.pull.PartyIdExtractor;
import eu.domibus.ebms3.common.model.UserMessage;

public interface PullService {


    /**
     * Search if a message ready for being pull exists for that initiator/mpc combination.
     * If it exists the message will be locked until the end of the transaction.
     *
     * @param initiator the party initiating the pull request.
     * @param mpc the mpc contained in the pull request.
     * @return the id of the message or null.
     */
    String getPullMessageId(String initiator, String mpc);

    /**
     * When a message arrives in the system, if it is configured to be pulled, some extra information needed for finding
     * the message later will be extracted and saved in a different place where the message lock will be facilitated.
     * @param partyIdExtractor interface allowing to retrieve the initiator information from different context.
     * @param userMessage
     * @param messageLog
     */
    void addSearchInFormation(PartyIdExtractor partyIdExtractor, UserMessage userMessage, MessageLog messageLog);

    /**
     * When a message has been successfully delivered or marked a failed, it should be removed from
     * the  temporary locking purpose information system.
     *
     * @param messageId the id of the message to be deleted;
     */
    void delete(String messageId);
}
