package eu.domibus.ext.services;

import eu.domibus.ext.services.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.services.exceptions.MessageAcknowledgeException;

import java.util.List;
import java.util.Map;

/**
 * Service responsible for managing message acknowledgments.
 *
 * @author Cosmin Baciu
 * @since 3.3
 *
 */
public interface MessageAcknowledgeService {

    /**
     * Registers an acknowledgment for a specific message
     *
     * @param messageId The message id for which the acknowledgement is registered
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    void acknowledgeMessage(String messageId, Map<String, String> properties) throws MessageAcknowledgeException;

    /**
     * Gets all acknowledgments for a specific message id
     *
     * @param messageId The message id for which message acknowledgments are retrieved
     * @return All acknowledgments registered for a specific message
     */
    List<MessageAcknowledgementDTO> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException;
}