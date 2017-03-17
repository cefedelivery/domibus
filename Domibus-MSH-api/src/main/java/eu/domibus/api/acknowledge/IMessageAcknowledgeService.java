package eu.domibus.api.acknowledge;

import java.util.List;

/**
 * Created by migueti on 15/03/2017.
 */
public interface IMessageAcknowledgeService {

    void acknowledgeMessage(String messageId) throws MessageAcknowledgeException;

    List<MessageAcknowledge> getAcknowledgeMessages(String messageId) throws MessageAcknowledgeException;
}