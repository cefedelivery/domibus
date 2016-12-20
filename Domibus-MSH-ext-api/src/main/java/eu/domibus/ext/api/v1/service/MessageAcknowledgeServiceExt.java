package eu.domibus.ext.api.v1.service;

import eu.domibus.ext.api.v1.domain.MessageAcknowledgeExt;
import eu.domibus.ext.api.v1.exception.MessageAcknowledgeExceptionExt;

import java.util.List;

/**
 * @author baciu
 */
public interface MessageAcknowledgeServiceExt {

    void acknowledgeMessage(String messageId) throws MessageAcknowledgeExceptionExt;

    List<MessageAcknowledgeExt> getMessagesAcknowledged(String messageId);
}
