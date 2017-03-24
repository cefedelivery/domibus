package eu.domibus.ext.delegate;

import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import eu.domibus.ext.services.MessageAcknowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageAcknowledgeServiceDelegate implements MessageAcknowledgeService {

    @Autowired
    MessageAcknowledgeService messageAcknowledgeService;

    @Autowired
    DomibusDomainConverter domibusDomainConverter;

    @Override
    public MessageAcknowledgementDTO acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) throws MessageAcknowledgeException {
        return null;
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to) throws MessageAcknowledgeException {
        return null;
    }

    @Override
    public List<MessageAcknowledgementDTO> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException {
        return null;
    }
}
