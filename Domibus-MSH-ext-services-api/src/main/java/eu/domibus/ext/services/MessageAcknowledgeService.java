package eu.domibus.ext.services;

import eu.domibus.ext.domain.MessageAcknowledgeDTO;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;

import java.util.List;

/**
 * @author  migueti, Cosmin Baciu
 * @since 3.3
 */
public interface MessageAcknowledgeService {

    void acknowledgeMessage(MessageAcknowledgeDTO messageAcknowledge) throws MessageAcknowledgeException;

    List<MessageAcknowledgeDTO> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException;
}
