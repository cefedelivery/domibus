package eu.domibus.ext.services;

import eu.domibus.ext.MessageAcknowledgeDTO;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;

import java.util.List;

/**
 * Created by migueti on 15/03/2017.
 */
public interface IMessageAcknowledgeService {

    void acknowledgeMessage(MessageAcknowledgeDTO messageAcknowledge) throws MessageAcknowledgeException;

    List<MessageAcknowledgeDTO> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException;
}
