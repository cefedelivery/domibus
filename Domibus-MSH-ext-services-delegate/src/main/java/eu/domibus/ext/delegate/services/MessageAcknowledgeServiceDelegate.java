package eu.domibus.ext.delegate.services;

import eu.domibus.api.acknowledge.MessageAcknowledgement;
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
    eu.domibus.api.acknowledge.MessageAcknowledgeService messageAcknowledgeCoreService;


    @Autowired
    DomibusDomainConverter domainConverter;

    @Autowired
    AuthUtils authUtils;

    //TODO move this into an interceptor so that it can be reusable
    protected void checkSecurity() throws AuthenticationException {
        try {
            if (!authUtils.isUnsecureLoginAllowed()) {
                authUtils.hasUserOrAdminRole();
            }
        } catch (eu.domibus.api.security.AuthenticationException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) throws AuthenticationException, MessageAcknowledgeException {
        checkSecurity();

        //TODO move the exception mapping into an interceptor
        try {
            final MessageAcknowledgement messageAcknowledgement = messageAcknowledgeCoreService.acknowledgeMessage(messageId, acknowledgeTimestamp, from, to, properties);
            return domainConverter.convert(messageAcknowledgement);
        } catch (Exception e) {
            throw new MessageAcknowledgeException(e);
        }

    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to) throws AuthenticationException, MessageAcknowledgeException {
        checkSecurity();
        return acknowledgeMessage(messageId, acknowledgeTimestamp, from, to);
    }

    @Override
    public List<MessageAcknowledgementDTO> getAcknowledgedMessages(String messageId) throws AuthenticationException, MessageAcknowledgeException {
        checkSecurity();
        try {
            final List<MessageAcknowledgement> messageAcknowledgement = messageAcknowledgeCoreService.getAcknowledgedMessages(messageId);
            return domainConverter.convert(messageAcknowledgement);
        } catch (Exception e) {
            throw new MessageAcknowledgeException(e);
        }
    }
}
