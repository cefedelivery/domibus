package eu.domibus.core.acknowledge;

import eu.domibus.api.acknowledge.MessageAcknowledgeException;
import eu.domibus.api.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.acknowledge.MessageAcknowledgement;
import eu.domibus.api.security.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageAcknowledgeDefaultService implements MessageAcknowledgeService {

    @Autowired
    MessageAcknowledgementDao messageAcknowledgementDao;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    MessageAcknowledgeConverter messageAcknowledgeConverter;

    @Override
    public MessageAcknowledgement acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) throws MessageAcknowledgeException {
        final String user = authUtils.getAuthenticatedUser();
        MessageAcknowledgementEntity entity = messageAcknowledgeConverter.create(user, messageId, acknowledgeTimestamp, from, to, properties);
        messageAcknowledgementDao.create(entity);
        return messageAcknowledgeConverter.convert(entity);
    }

    @Override
    public MessageAcknowledgement acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to) throws MessageAcknowledgeException {
        return acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, null);
    }



    @Override
    public List<MessageAcknowledgement> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException {
        final List<MessageAcknowledgementEntity> entities = messageAcknowledgementDao.findByMessageId(messageId);
        return messageAcknowledgeConverter.convert(entities);
    }


}
