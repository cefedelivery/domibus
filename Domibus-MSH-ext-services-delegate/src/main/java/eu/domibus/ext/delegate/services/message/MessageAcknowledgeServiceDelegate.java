package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.delegate.services.security.SecurityService;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.MessageAcknowledgeExtException;
import eu.domibus.ext.services.MessageAcknowledgeExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
public class MessageAcknowledgeServiceDelegate implements MessageAcknowledgeExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgeServiceDelegate.class);

    @Autowired
    eu.domibus.api.message.acknowledge.MessageAcknowledgeService messageAcknowledgeCoreService;

    @Autowired
    DomainExtConverter domainConverter;

    @Autowired
    SecurityService securityService;


    @Override
    public MessageAcknowledgementDTO acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationExtException, MessageAcknowledgeExtException {
        securityService.checkMessageAuthorization(messageId);

        final MessageAcknowledgement messageAcknowledgement = messageAcknowledgeCoreService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, properties);
        return domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp) throws AuthenticationExtException, MessageAcknowledgeExtException {
        return acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, null);
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationExtException, MessageAcknowledgeExtException {
        securityService.checkMessageAuthorization(messageId);

        final MessageAcknowledgement messageAcknowledgement = messageAcknowledgeCoreService.acknowledgeMessageProcessed(messageId, acknowledgeTimestamp, properties);
        return domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp) throws AuthenticationExtException, MessageAcknowledgeExtException {
        return acknowledgeMessageProcessed(messageId, acknowledgeTimestamp, null);
    }

    @Override
    public List<MessageAcknowledgementDTO> getAcknowledgedMessages(String messageId) throws AuthenticationExtException, MessageAcknowledgeExtException {
        securityService.checkMessageAuthorization(messageId);

        final List<MessageAcknowledgement> messageAcknowledgement = messageAcknowledgeCoreService.getAcknowledgedMessages(messageId);
        return domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);

    }

}
