package eu.domibus.core.acknowledge;

import eu.domibus.api.acknowledge.MessageAcknowledgeException;
import eu.domibus.api.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.acknowledge.MessageAcknowledgement;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.core.message.MessageServiceHelper;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageAcknowledgeDefaultService implements MessageAcknowledgeService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgeDefaultService.class);

    @Autowired
    MessageAcknowledgementDao messageAcknowledgementDao;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    MessagingDao messagingDao;

    @Autowired
    MessageAcknowledgeConverter messageAcknowledgeConverter;

    @Autowired
    MessageServiceHelper messageServiceHelper;


    @Override
    public MessageAcknowledgement acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationException, MessageAcknowledgeException {
        final UserMessage userMessage = getUserMessage(messageId);
        final String localAccessPointId = getLocalAccessPointId(userMessage);
        final String finalRecipient = messageServiceHelper.getFinalRecipient(userMessage);
        return acknowledgeMessage(userMessage, acknowledgeTimestamp, localAccessPointId, finalRecipient, properties);
    }

    private UserMessage getUserMessage(String messageId) {
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageAcknowledgeException(DomibusCoreErrorCode.DOM_001, "Message with ID [" + messageId + "] does not exist");
        }
        return userMessage;
    }

    //TODO move this into an interceptor so that it can be reusable

    /**
     * Checks if the authenticated user has the rights to perform an acknowledge on the message
     * @param userMessage
     * @throws eu.domibus.ext.exceptions.AuthenticationException
     */
    protected void checkSecurity(final UserMessage userMessage) throws eu.domibus.ext.exceptions.AuthenticationException {
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
        final boolean sameFinalRecipient = messageServiceHelper.isSameFinalRecipient(userMessage, originalUserFromSecurityContext);
        if (!sameFinalRecipient) {
            //TODO transform to security log
            LOG.debug("User [{}] is trying to submit/access a message having as final recipient: [{}]", originalUserFromSecurityContext, messageServiceHelper.getFinalRecipient(userMessage));
            throw new AuthenticationException("You are not allowed to handle this message. You are authorized as [" + originalUserFromSecurityContext + "]");
        }
    }


    @Override
    public MessageAcknowledgement acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp) throws AuthenticationException, MessageAcknowledgeException {
        return acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, null);
    }

    @Override
    public MessageAcknowledgement acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationException, MessageAcknowledgeException {
        final UserMessage userMessage = getUserMessage(messageId);
        final String localAccessPointId = getLocalAccessPointId(userMessage);
        final String finalRecipient = messageServiceHelper.getFinalRecipient(userMessage);
        return acknowledgeMessage(userMessage, acknowledgeTimestamp, finalRecipient, localAccessPointId, properties);
    }

    @Override
    public MessageAcknowledgement acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp) throws AuthenticationException, MessageAcknowledgeException {
        return acknowledgeMessageProcessed(messageId, acknowledgeTimestamp, null);
    }


    public MessageAcknowledgement acknowledgeMessage(final UserMessage userMessage, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) throws MessageAcknowledgeException {
        final String messageId = userMessage.getMessageInfo().getMessageId();
        //throw exception in case the message id does not exist
        if (userMessage == null) {
            throw new MessageAcknowledgeException(DomibusCoreErrorCode.DOM_001, "Message with ID [" + messageId + "] does not exist");
        }

        checkSecurity(userMessage);

        final String user = authUtils.getAuthenticatedUser();
        MessageAcknowledgementEntity entity = messageAcknowledgeConverter.create(user, messageId, acknowledgeTimestamp, from, to, properties);
        messageAcknowledgementDao.create(entity);
        return messageAcknowledgeConverter.convert(entity);
    }

    @Override
    public List<MessageAcknowledgement> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException {
        final UserMessage userMessage = getUserMessage(messageId);
        checkSecurity(userMessage);

        final List<MessageAcknowledgementEntity> entities = messageAcknowledgementDao.findByMessageId(messageId);
        return messageAcknowledgeConverter.convert(entities);
    }

    private String getLocalAccessPointId(UserMessage userMessage) {
        //TODO check why there are multiple party ids instead of just one
        final Set<PartyId> partyId = userMessage.getPartyInfo().getTo().getPartyId();
        if (partyId == null || partyId.isEmpty()) {
            return null;
        }
        return partyId.iterator().next().getValue();
    }

}
