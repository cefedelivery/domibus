package eu.domibus.service.acknowledge;

/**
 * @author Cosmin Baciu
 */
public interface MessageAcknowledgeService {

    void acknowledgeMessage(String messageId) throws MessageAcknowledgeException;

    boolean isMessageAcknowledged(String messageId);
}
