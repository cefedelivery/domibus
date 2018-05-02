package eu.domibus.core.pull;

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * In the case of a pull mechanism when a message is retrieved, there is a need for a lock mechanism to occur in order
 * to avoid that a message is pulled twice. This service is in charge of this behavior.
 */
public interface MessagingLockService {

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
     *
     * @param partyIdExtractor interface allowing to retrieve the initiator information from different context.
     * @param messageId the id of the message.
     * @param mpc the mpc of the message.
     */
    void addSearchInFormation(PartyIdExtractor partyIdExtractor, String messageId, String mpc);

    /**
     * When a message has been successfully delivered or marked a failed, it should be removed from
     * the  temporary locking purpose information system.
     *
     * @param messageId the id of the message to be deleted;
     */
    void delete(String messageId);

}
