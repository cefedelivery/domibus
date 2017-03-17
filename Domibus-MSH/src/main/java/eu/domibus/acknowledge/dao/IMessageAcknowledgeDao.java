package eu.domibus.acknowledge.dao;

import eu.domibus.acknowledge.entities.MessageAcknowledge;

/**
 * Created by migueti on 15/03/2017.
 */
public interface IMessageAcknowledgeDao {

    MessageAcknowledge findByCriteria(String messageId, String username, String originalUser);
}
