package eu.domibus.ext.delegate.services;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.ext.delegate.converter.DomibusDomainConverter;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import eu.domibus.ext.services.MessageAcknowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageAcknowledgeServiceDelegate implements MessageAcknowledgeService {

    @Autowired
    eu.domibus.api.message.acknowledge.MessageAcknowledgeService messageAcknowledgeCoreService;


    @Autowired
    DomibusDomainConverter domainConverter;

    @Autowired
    AuthUtils authUtils;

    @Override
    public MessageAcknowledgementDTO acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationException, MessageAcknowledgeException {
        //TODO move the exception mapping into an interceptor
        try {
            final MessageAcknowledgement messageAcknowledgement = messageAcknowledgeCoreService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, properties);
            return domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);
        } catch (eu.domibus.api.security.AuthenticationException e) {
            throw new AuthenticationException(e);
        } catch (Exception e) {
            throw new MessageAcknowledgeException(e);
        }
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp) throws AuthenticationException, MessageAcknowledgeException {
        return acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, null);
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationException, MessageAcknowledgeException {
        //TODO move the exception mapping into an interceptor
        try {
            final MessageAcknowledgement messageAcknowledgement = messageAcknowledgeCoreService.acknowledgeMessageProcessed(messageId, acknowledgeTimestamp, properties);
            return domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);
        } catch (eu.domibus.api.security.AuthenticationException e) {
            throw new AuthenticationException(e);
        } catch (Exception e) {
            throw new MessageAcknowledgeException(e);
        }
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp) throws AuthenticationException, MessageAcknowledgeException {
        return acknowledgeMessageProcessed(messageId, acknowledgeTimestamp, null);
    }

    @Override
    public List<MessageAcknowledgementDTO> getAcknowledgedMessages(String messageId) throws AuthenticationException, MessageAcknowledgeException {
        try {
            final List<MessageAcknowledgement> messageAcknowledgement = messageAcknowledgeCoreService.getAcknowledgedMessages(messageId);
            return domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);
        } catch (eu.domibus.api.security.AuthenticationException e) {
            throw new AuthenticationException(e);
        } catch (Exception e) {
            throw new MessageAcknowledgeException(e);
        }
    }
}
