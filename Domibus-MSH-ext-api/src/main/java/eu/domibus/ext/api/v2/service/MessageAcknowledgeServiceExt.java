package eu.domibus.ext.api.v2.service;

import eu.domibus.ext.api.v2.domain.MessageAcknowledgeExt;
import eu.domibus.ext.api.v2.exception.MessageAcknowledgeExceptionExt;

import java.util.List;

/**
 * @author baciu
 */
public interface MessageAcknowledgeServiceExt {

    void acknowledgeMessage(String messageId) throws MessageAcknowledgeExceptionExt;

    List<MessageAcknowledgeExt> getMessagesAcknowledged(String messageId);
}
