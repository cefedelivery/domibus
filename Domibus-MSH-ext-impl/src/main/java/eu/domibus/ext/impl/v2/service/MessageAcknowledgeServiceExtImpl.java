package eu.domibus.ext.impl.v2.service;

import eu.domibus.api.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.domain.MessageAcknowledge;
import eu.domibus.ext.api.v2.domain.MessageAcknowledgeExt;
import eu.domibus.ext.api.v2.exception.MessageAcknowledgeExceptionExt;
import eu.domibus.ext.api.v2.service.MessageAcknowledgeServiceExt;
import eu.domibus.ext.impl.v2.converter.DomibusDomainConverter;
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
        messageAcknowledgeService.acknowledgeMessage(messageId);
    }

    @Override
    public List<MessageAcknowledgeExt> getMessagesAcknowledged(String messageId) {
        final List<MessageAcknowledge> messagesAcknowledged = messageAcknowledgeService.getMessagesAcknowledged(messageId);
        return domibusDomainConverter.convert(messagesAcknowledged);
    }
}
