package eu.domibus.ext.impl.v1.service;

import eu.domibus.api.domain.MessageAcknowledge;
import eu.domibus.ext.api.v1.domain.MessageAcknowledgeDTO;
import eu.domibus.ext.api.v1.exception.MessageAcknowledgeException;
import eu.domibus.ext.api.v1.service.MessageAcknowledgeService;
import eu.domibus.ext.impl.v1.converter.DomibusDomainConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author baciu
 */
@Component
public class MessageAcknowledgeServiceExtImpl implements MessageAcknowledgeService {

    @Autowired
    eu.domibus.api.acknowledge.MessageAcknowledgeService messageAcknowledgeService;

    @Autowired
    DomibusDomainConverter domibusDomainConverter;

    @Override
    public void acknowledgeMessage(String messageId) throws MessageAcknowledgeException {
        messageAcknowledgeService.acknowledgeMessage(messageId);
    }

    @Override
    public List<MessageAcknowledgeExt> getAcknowledgedMessages(String messageId) {
        final List<MessageAcknowledge> messagesAcknowledged = messageAcknowledgeService.getMessagesAcknowledged(messageId);
        return domibusDomainConverter.convert(messagesAcknowledged);
    }
}
