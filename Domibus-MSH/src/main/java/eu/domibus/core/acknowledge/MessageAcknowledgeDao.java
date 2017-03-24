package eu.domibus.core.acknowledge;

import eu.domibus.common.model.configuration.MessageAcknowledgementEntity;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
public interface MessageAcknowledgeDao {

    MessageAcknowledgementEntity findByMessageId(String messageId);

    MessageAcknowledgementEntity findByFrom(String from);

    MessageAcknowledgementEntity findByTo(String to);
}
