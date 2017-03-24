package eu.domibus.api.acknowledge;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
public interface MessageAcknowledgeService {

    /**
     * Registers an acknowledgment for a specific message using the provided properties
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @param from                 The source of the acknowledgement(eg: C3 or backend name)
     * @param to                   The target of the acknowledgement(eg: C3 or backend name)
     * @param properties           Custom properties of the message acknowledgment
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    MessageAcknowledgement acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) throws MessageAcknowledgeException;


    /**
     * Registers an acknowledgment for a specific message
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @param from                 The source of the acknowledgement(eg: C3 or backend name)
     * @param to                   The target of the acknowledgement(eg: C3 or backend name)
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    MessageAcknowledgement acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to) throws MessageAcknowledgeException;

    /**
     * Gets all acknowledgments associated to a message id
     *
     * @param messageId The message id for which message acknowledgments are retrieved
     * @return All acknowledgments registered for a specific message
     */
    List<MessageAcknowledgement> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException;
}