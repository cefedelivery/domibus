package eu.domibus.ext.impl.v1.service;

import eu.domibus.api.acknowledge.MessageAcknowledgeException;
import eu.domibus.api.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.domain.MessageAcknowledge;
import eu.domibus.ext.api.v1.domain.MessageAcknowledgeExt;
import eu.domibus.ext.api.v1.exception.MessageAcknowledgeExceptionExt;
import eu.domibus.ext.api.v1.service.MessageAcknowledgeServiceExt;
import eu.domibus.ext.impl.v1.converter.DomibusDomainConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author baciu
 */
@Component
public class MessageAcknowledgeServiceExtImpl implements MessageAcknowledgeServiceExt {

    @Autowired
    MessageAcknowledgeService messageAcknowledgeService;

    @Autowired
    DomibusDomainConverter domibusDomainConverter;

    @Override
    public void acknowledgeMessage(String messageId) throws MessageAcknowledgeExceptionExt {
        try {
            messageAcknowledgeService.acknowledgeMessage(messageId);
        } catch (MessageAcknowledgeException e) {
            throw new MessageAcknowledgeExceptionExt(e);
        }

    }

    @Override
    public List<MessageAcknowledgeExt> getMessagesAcknowledged(String messageId) {
        final List<MessageAcknowledge> messagesAcknowledged = messageAcknowledgeService.getMessagesAcknowledged(messageId);
        return domibusDomainConverter.convert(messagesAcknowledged);
    }
}
