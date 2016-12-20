package eu.domibus.api.acknowledge;

import eu.domibus.api.domain.MessageAcknowledge;

import java.util.List;

/**
 * @author Cosmin Baciu
 */
public interface MessageAcknowledgeService {

    void acknowledgeMessage(String messageId) throws MessageAcknowledgeException;

    List<MessageAcknowledge> getMessagesAcknowledged(String messageId);
}
