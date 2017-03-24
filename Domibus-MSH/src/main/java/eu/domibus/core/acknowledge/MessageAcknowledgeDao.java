package eu.domibus.core.acknowledge;

import eu.domibus.common.model.configuration.MessageAcknowledge;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
public interface MessageAcknowledgeDao {

    MessageAcknowledge findByMessageId(String messageId);

    MessageAcknowledge findByFrom(String from);

    MessageAcknowledge findByTo(String to);
}
