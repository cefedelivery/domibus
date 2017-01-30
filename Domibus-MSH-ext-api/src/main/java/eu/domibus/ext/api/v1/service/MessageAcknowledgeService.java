package eu.domibus.ext.api.v1.service;

import eu.domibus.ext.api.v1.domain.MessageAcknowledgeDTO;
import eu.domibus.ext.api.v1.exception.MessageAcknowledgeException;

import java.util.List;

/**
 * @author Cosmin Baciu
 *
 * This class is responsible for managing message acknowledgments
 */
public interface MessageAcknowledgeService {

    /**
     * Registers an acknowledgment for a specific message
     *
     * @param messageAcknowledgeDTO Contains the details of the message acknowledgment
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    void acknowledgeMessage(MessageAcknowledgeDTO messageAcknowledgeDTO) throws MessageAcknowledgeException;

    /**
     * Gets all acknowledgments for a specific message id
     *
     * @param messageId The message id for which message acknowledgments are retrieved
     * @return All acknowledgments for a specific message
     */
    List<MessageAcknowledgeDTO> getAcknowledgedMessages(String messageId);
}
