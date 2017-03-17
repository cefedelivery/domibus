package eu.domibus.ext.delegate;

import eu.domibus.ext.services.IMessageAcknowledgeService;
import eu.domibus.ext.MessageAcknowledgeDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by migueti on 15/03/2017.
 */
public class MessageAcknowledgeResource {

    @Autowired
    IMessageAcknowledgeService messageAcknowledgeService;

    void acknowledgeMessage(MessageAcknowledgeDTO messageAcknowledge) {
        messageAcknowledgeService.acknowledgeMessage(messageAcknowledge);
    }

    List<MessageAcknowledgeDTO> getMessagesAcknowledged(String messageId) {
        return messageAcknowledgeService.getAcknowledgedMessages(messageId);
    }
}
