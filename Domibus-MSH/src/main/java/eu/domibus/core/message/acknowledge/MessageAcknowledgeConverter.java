package eu.domibus.core.message.acknowledge;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface MessageAcknowledgeConverter {

    MessageAcknowledgementEntity create(String user, String messageId, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties);

    MessageAcknowledgement convert(MessageAcknowledgementEntity entity);

    List<MessageAcknowledgement> convert(List<MessageAcknowledgementEntity> entities);
}
