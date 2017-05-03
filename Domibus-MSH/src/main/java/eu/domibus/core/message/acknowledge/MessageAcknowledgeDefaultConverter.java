package eu.domibus.core.message.acknowledge;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class MessageAcknowledgeDefaultConverter implements MessageAcknowledgeConverter {

    @Override
    public MessageAcknowledgementEntity create(String user, String messageId, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) {
        MessageAcknowledgementEntity result = new MessageAcknowledgementEntity();
        result.setMessageId(messageId);
        result.setAcknowledgeDate(acknowledgeTimestamp);
        result.setCreateDate(new Timestamp(System.currentTimeMillis()));
        result.setCreateUser(user);
        result.setFrom(from);
        result.setTo(to);
        result.setPropertiesWithMap(properties);
        return result;
    }

    @Override
    public MessageAcknowledgement convert(MessageAcknowledgementEntity entity) {
        MessageAcknowledgement result = new MessageAcknowledgement();
        result.setId(entity.getEntityId());
        result.setMessageId(entity.getMessageId());
        result.setFrom(entity.getFrom());
        result.setTo(entity.getTo());
        result.setCreateDate(entity.getCreateDate());
        result.setCreateUser(entity.getCreateUser());
        result.setAcknowledgeDate(entity.getAcknowledgeDate());
        result.setProperties(entity.getPropertiesAsMap());
        return result;
    }

    @Override
    public List<MessageAcknowledgement> convert(List<MessageAcknowledgementEntity> entities) {
        if (entities == null) {
            return null;
        }
        List<MessageAcknowledgement> result = new ArrayList<>();
        for (MessageAcknowledgementEntity entity : entities) {
            result.add(convert(entity));
        }
        return result;
    }
}
