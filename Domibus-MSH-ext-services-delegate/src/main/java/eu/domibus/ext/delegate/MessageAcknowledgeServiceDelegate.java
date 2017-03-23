package eu.domibus.ext.delegate;

import eu.domibus.ext.domain.MessageAcknowledgeDTO;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import eu.domibus.ext.services.MessageAcknowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author  migueti, Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageAcknowledgeServiceDelegate implements MessageAcknowledgeService {

    @Autowired
    MessageAcknowledgeService messageAcknowledgeService;

    @Autowired
    DomibusDomainConverter domibusDomainConverter;

    @Override
    public void acknowledgeMessage(MessageAcknowledgeDTO messageAcknowledge) throws MessageAcknowledgeException {

    }

    @Override
    public List<MessageAcknowledgeDTO> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException {
        return null;
    }
}
