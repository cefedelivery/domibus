package eu.domibus.ext.delegate.services;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.ebms3.UserMessageService;
import eu.domibus.api.message.ebms3.UserMessageServiceHelper;
import eu.domibus.api.message.ebms3.model.UserMessage;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.ext.delegate.converter.DomibusDomainConverter;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import eu.domibus.ext.services.MessageAcknowledgeService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
    UserMessageServiceHelper userMessageServiceHelper;

    @Autowired
    UserMessageService userMessageService;


    @Autowired
    DomibusDomainConverter domainConverter;

    @Autowired
    AuthUtils authUtils;



    @Override
    public MessageAcknowledgementDTO acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationException, MessageAcknowledgeException {
        checkSecurity(messageId);
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
        checkSecurity(messageId);
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
        checkSecurity(messageId);
        try {
            final List<MessageAcknowledgement> messageAcknowledgement = messageAcknowledgeCoreService.getAcknowledgedMessages(messageId);
            return domainConverter.convert(messageAcknowledgement, MessageAcknowledgementDTO.class);
        } catch (eu.domibus.api.security.AuthenticationException e) {
            throw new AuthenticationException(e);
        } catch (Exception e) {
            throw new MessageAcknowledgeException(e);
        }
    }

    private UserMessage getUserMessage(String messageId) {
        final UserMessage userMessage = userMessageService.getMessage(messageId);
        if (userMessage == null) {
            throw new MessageAcknowledgeException(DomibusErrorCode.DOM_001, "Message with ID [" + messageId + "] does not exist");
        }
        return userMessage;
    }

    //TODO move this into an interceptor so that it can be reusable

    /**
     * Checks if the authenticated user has the rights to perform an acknowledge on the message
     *
     * @param messageId
     * @throws eu.domibus.ext.exceptions.AuthenticationException
     */
    protected void checkSecurity(final String messageId) throws eu.domibus.ext.exceptions.AuthenticationException {
        final UserMessage userMessage = getUserMessage(messageId);
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
        final boolean sameFinalRecipient = userMessageServiceHelper.isSameFinalRecipient(userMessage, originalUserFromSecurityContext);
        if (!sameFinalRecipient) {
            //TODO transform to security log
            LOG.debug("User [{}] is trying to submit/access a message having as final recipient: [{}]", originalUserFromSecurityContext, userMessageServiceHelper.getFinalRecipient(userMessage));
            throw new eu.domibus.api.security.AuthenticationException("You are not allowed to handle this message. You are authorized as [" + originalUserFromSecurityContext + "]");
        }
    }
}
