package eu.domibus.core.pull;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface MessagingLockService {

    String getPullMessageToProcess(String initiator, String mpc);

    void addLockingInformation(PartyIdExtractor partyIdExtractor, String messageId, String mpc);

    void delete(String messageId);

}
