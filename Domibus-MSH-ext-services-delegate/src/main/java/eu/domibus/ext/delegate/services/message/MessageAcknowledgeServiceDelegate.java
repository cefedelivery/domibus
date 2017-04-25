package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.ext.delegate.converter.DomibusDomainConverter;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import eu.domibus.ext.services.MessageAcknowledgeService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgeServiceDelegate.class);

    @Autowired
    eu.domibus.api.message.acknowledge.MessageAcknowledgeService messageAcknowledgeCoreService;

    @Autowired
    UserMessageService userMessageService;


    @Autowired
    DomibusDomainConverter domainConverter;

    @Autowired
    AuthUtils authUtils;


    @Override
    public MessageAcknowledgementDTO acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationException, MessageAcknowledgeException {
        checkSecurity(messageId);

        final MessageAcknowledgement messageAcknowledgement = messageAcknowledgeCoreService.acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, properties);
        return domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp) throws AuthenticationException, MessageAcknowledgeException {
        return acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, null);
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationException, MessageAcknowledgeException {
        checkSecurity(messageId);

        final MessageAcknowledgement messageAcknowledgement = messageAcknowledgeCoreService.acknowledgeMessageProcessed(messageId, acknowledgeTimestamp, properties);
        return domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);
    }

    @Override
    public MessageAcknowledgementDTO acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp) throws AuthenticationException, MessageAcknowledgeException {
        return acknowledgeMessageProcessed(messageId, acknowledgeTimestamp, null);
    }

    @Override
    public List<MessageAcknowledgementDTO> getAcknowledgedMessages(String messageId) throws AuthenticationException, MessageAcknowledgeException {
        checkSecurity(messageId);
        final List<MessageAcknowledgement> messageAcknowledgement = messageAcknowledgeCoreService.getAcknowledgedMessages(messageId);
        return domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);

    }

    /**
     * Checks if the authenticated user has the rights to perform an acknowledge on the message
     *
     * @param messageId
     * @throws eu.domibus.ext.exceptions.AuthenticationException
     */
    protected void checkSecurity(final String messageId) throws eu.domibus.ext.exceptions.AuthenticationException {
        final String finalRecipient = userMessageService.getFinalRecipient(messageId);
        if (finalRecipient == null) {
            throw new MessageAcknowledgeException(DomibusErrorCode.DOM_001, "Couldn't get the finalRecipient for message with ID [" + messageId + "]");
        }

        if (authUtils.isUnsecureLoginAllowed()) {
            LOG.debug("Unsecured login allowed; no security checks will be done");
            return;
        }

        if (authUtils.isUserAdmin()) {
            LOG.debug("User [{}] has admin role, no other security checks will be done", authUtils.getAuthenticatedUser());
            return;
        }

        try {
            //check if the authenticated user has user role
            authUtils.hasUserRole();
        } catch (AccessDeniedException e) {
            throw new eu.domibus.ext.exceptions.AuthenticationException(e);
        }

        final String originalUserFromSecurityContext = authUtils.getOriginalUserFromSecurityContext();
        if (StringUtils.equals(finalRecipient, originalUserFromSecurityContext)) {
            LOG.debug("For message [{}] the provided final recipient [{}] is the same as the message final recipient", messageId, originalUserFromSecurityContext);
        } else {
            LOG.securityInfo(DomibusMessageCode.SEC_UNAUTHORIZED_MESSAGE_ACCESS, originalUserFromSecurityContext, finalRecipient);
            throw new AuthenticationException(DomibusErrorCode.DOM_002, "You are not allowed to handle this message. You are authorized as [" + originalUserFromSecurityContext + "]");
        }
    }
}
