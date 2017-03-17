package eu.domibus.ext.delegate;

import eu.domibus.ext.services.IMessageAcknowledgeService;
import eu.domibus.ext.MessageAcknowledgeDTO;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by migueti on 15/03/2017.
 */
@Service(value = "messageAcknowledgeServiceDelegate")
public class MessageAcknowledgeServiceDelegate implements IMessageAcknowledgeService {

    @Autowired
    IMessageAcknowledgeService messageAcknowledgeService;

    @Autowired
    IDomibusDomainConverter domibusDomainConverter;

    @Override
    public void acknowledgeMessage(MessageAcknowledgeDTO messageAcknowledge) throws MessageAcknowledgeException {

    }

    @Override
    public List<MessageAcknowledgeDTO> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException {
        return null;
    }
}
