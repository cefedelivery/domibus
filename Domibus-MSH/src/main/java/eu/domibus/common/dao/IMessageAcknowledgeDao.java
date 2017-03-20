package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.MessageAcknowledge;

/**
 * Created by migueti on 15/03/2017.
 */
public interface IMessageAcknowledgeDao {

    MessageAcknowledge findByMessageId(String messageId);

    MessageAcknowledge findByFrom(String from);

    MessageAcknowledge findByTo(String to);
}
