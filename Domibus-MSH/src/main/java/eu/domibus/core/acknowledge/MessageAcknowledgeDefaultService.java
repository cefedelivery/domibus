package eu.domibus.core.acknowledge;

import eu.domibus.api.acknowledge.MessageAcknowledgeException;
import eu.domibus.api.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.acknowledge.MessageAcknowledgement;
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
public class MessageAcknowledgeDefaultService implements MessageAcknowledgeService {

    @Autowired
    MessageAcknowledgementDao messageAcknowledgementDao;

    @Override
    public MessageAcknowledgement acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) throws MessageAcknowledgeException {
        return null;
    }

    @Override
    public MessageAcknowledgement acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to) throws MessageAcknowledgeException {
        return null;
    }

    @Override
    public List<MessageAcknowledgement> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException {
        return null;
    }
}
