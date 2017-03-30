package eu.domibus.core.acknowledge;

import eu.domibus.api.acknowledge.MessageAcknowledgeException;
import eu.domibus.api.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.acknowledge.MessageAcknowledgement;
import eu.domibus.ebms3.security.util.AuthUtils;
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

    @Override
    public MessageAcknowledgement acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) throws MessageAcknowledgeException {
        final String user = authUtils.getAuthenticatedUser();
        MessageAcknowledgementEntity entity = create(user, messageId, acknowledgeTimestamp, from, to, properties);
        messageAcknowledgementDao.create(entity);
        return convert(entity);
    }

    protected MessageAcknowledgementEntity create(String user, String messageId, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) {
        MessageAcknowledgementEntity result = new MessageAcknowledgementEntity();
        result.setMessageId(messageId);
        result.setAcknowledgeDate(acknowledgeTimestamp);
        result.setCreateDate(new Timestamp(System.currentTimeMillis()));
        result.setCreateUser(user);
        result.setFrom(from);
        result.setTo(to);
        if (properties != null && !properties.isEmpty()) {
            Set<MessageAcknowledgementProperty> acknowledgmentProperties = new HashSet<>();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                MessageAcknowledgementProperty property = new MessageAcknowledgementProperty();
                property.setName(entry.getKey());
                property.setValue(entry.getValue());
                acknowledgmentProperties.add(property);
            }
            result.setProperties(acknowledgmentProperties);
        }
        return result;
    }

    protected MessageAcknowledgement convert(MessageAcknowledgementEntity entity) {
        MessageAcknowledgement result = new MessageAcknowledgement();
        result.setId(entity.getEntityId());
        result.setMessageId(entity.getMessageId());
        result.setFrom(entity.getFrom());
        result.setTo(entity.getTo());
        result.setCreateDate(entity.getCreateDate());
        result.setCreateUser(entity.getCreateUser());
        result.setAcknowledgeDate(entity.getAcknowledgeDate());
        final Set<MessageAcknowledgementProperty> properties = entity.getProperties();

        Map<String, Object> propertiesHashMap = new HashMap<>();
        for (MessageAcknowledgementProperty property : properties) {
            propertiesHashMap.put(property.getName(), property.getValue());
        }
        result.setProperties(propertiesHashMap);
        return result;
    }

    protected List<MessageAcknowledgement> convert(List<MessageAcknowledgementEntity> entities) {
        if (entities == null) {
            return null;
        }
        List<MessageAcknowledgement> result = new ArrayList<>();
        for (MessageAcknowledgementEntity entity : entities) {
            result.add(convert(entity));
        }
        return result;
    }

    @Override
    public MessageAcknowledgement acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to) throws MessageAcknowledgeException {
        return acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, null);
    }

    @Override
    public List<MessageAcknowledgement> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException {
        final List<MessageAcknowledgementEntity> entities = messageAcknowledgementDao.findByMessageId(messageId);
        return convert(entities);
    }


}
