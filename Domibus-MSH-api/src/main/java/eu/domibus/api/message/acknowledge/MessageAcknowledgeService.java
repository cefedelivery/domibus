package eu.domibus.api.message.acknowledge;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
public interface MessageAcknowledgeService {

    /**
     * Acknowledges that a message has been delivered to the backend
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @param properties           Custom properties of the message acknowledgment
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    MessageAcknowledgement acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws MessageAcknowledgeException;

    /**
     * Acknowledges that a message has been delivered to the backend
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    MessageAcknowledgement acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp) throws MessageAcknowledgeException;


    /**
     * Acknowledges that a message has been processed by the backend
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @param properties           Custom properties of the message acknowledgment
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    MessageAcknowledgement acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws MessageAcknowledgeException;


    /**
     * Acknowledges that a message has been processed by the backend
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    MessageAcknowledgement acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp) throws MessageAcknowledgeException;

    /**
     * Gets all acknowledgments associated to a message id
     *
     * @param messageId The message id for which message acknowledgments are retrieved
     * @return All acknowledgments registered for a specific message
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to get the acknowledgments
     */
    List<MessageAcknowledgement> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException;
}