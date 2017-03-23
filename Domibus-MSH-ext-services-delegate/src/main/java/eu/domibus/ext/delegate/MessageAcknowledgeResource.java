package eu.domibus.ext.delegate;

import eu.domibus.ext.domain.MessageAcknowledgeDTO;
import eu.domibus.ext.services.MessageAcknowledgeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author  migueti, Cosmin Baciu
 * @since 3.3
 */
//TODO change this to a rest resource
public class MessageAcknowledgeResource {

    @Autowired
    MessageAcknowledgeService messageAcknowledgeService;

    public void acknowledgeMessage(MessageAcknowledgeDTO messageAcknowledge) {
        messageAcknowledgeService.acknowledgeMessage(messageAcknowledge);
    }

    public List<MessageAcknowledgeDTO> getMessagesAcknowledged(String messageId) {
        return messageAcknowledgeService.getAcknowledgedMessages(messageId);
    }
}
