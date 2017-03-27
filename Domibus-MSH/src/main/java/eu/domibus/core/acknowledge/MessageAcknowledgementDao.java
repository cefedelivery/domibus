package eu.domibus.core.acknowledge;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
public interface MessageAcknowledgementDao {

    MessageAcknowledgementEntity findByMessageId(String messageId);

    MessageAcknowledgementEntity findByFrom(String from);

    MessageAcknowledgementEntity findByTo(String to);
}
