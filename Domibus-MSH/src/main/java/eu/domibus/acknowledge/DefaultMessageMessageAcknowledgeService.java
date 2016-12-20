package eu.domibus.acknowledge;

import eu.domibus.common.dao.MessagingDao;
import eu.domibus.ebms3.common.model.MessageAcknowledge;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.service.acknowledge.MessageAcknowledgeException;
import eu.domibus.service.acknowledge.MessageAcknowledgeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;

/**
 * @author baciu
 */
@Component
public class DefaultMessageMessageAcknowledgeService implements MessageAcknowledgeService {

    private static final Log LOG = LogFactory.getLog(DefaultMessageMessageAcknowledgeService.class);

    @Autowired
    AuthUtils authUtils;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private MessageAcknowledgeDao messageAcknowledgeDao;

    @Override
    public void acknowledgeMessage(String messageId) throws MessageAcknowledgeException {
        checkAcknowledgeRights(messageId);

        String originalUser = authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
        String username = authUtils.getUsername();

        final MessageAcknowledge existingMessageAcknowledge = messageAcknowledgeDao.findByCriteria(messageId, username, originalUser);
        if (existingMessageAcknowledge != null) {
            throw new MessageAcknowledgeException("Message acknoledge already existing for messageId [" + messageId + "] , user [" + username + "], originalUser [" + originalUser + "]");
        }

        MessageAcknowledge messageAcknowledge = new MessageAcknowledge();
        //fill details
        messageAcknowledgeDao.create(messageAcknowledge);
    }

    @Override
    public boolean isMessageAcknowledged(String messageId) {
        //TODO
        return false;
    }

    protected void checkAcknowledgeRights(String messageId) {
        if (!authUtils.isUnsecureLoginAllowed()) {
            //if security is enabled just check that the user is authenticated
            authUtils.hasUserOrAdminRole();
        }

        String originalUser = authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
        LOG.debug("Authorized as " + (originalUser == null ? "super user" : originalUser));

        UserMessage userMessage;
        try {
            LOG.info("Searching message with id [" + messageId + "]");
            userMessage = messagingDao.findUserMessageByMessageId(messageId);

        } catch (final NoResultException nrEx) {
            LOG.debug("Message with id [" + messageId + "] was not found");
            throw new MessageAcknowledgeException("Message with id [" + messageId + "] was not found");
        }

        // Authorization check
        validateOriginalUser(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);

    }

    //copied from DatabaseMessageHandler; we should refactor this in order to make it re-usable
    private void validateOriginalUser(UserMessage userMessage, String authOriginalUser, String recipient) {
        if (authOriginalUser != null) {
            LOG.debug("OriginalUser is [" + authOriginalUser + "]");
            /* check the message belongs to the authenticated user */
            String originalUser = getOriginalUser(userMessage, recipient);
            if (originalUser != null && !originalUser.equals(authOriginalUser)) {
                LOG.debug("User [" + authOriginalUser + "] is trying to submit/access a message having as final recipient: " + originalUser);
                throw new AccessDeniedException("You are not allowed to handle this message. You are authorized as [" + authOriginalUser + "]");
            }
        }
    }

    //copied from DatabaseMessageHandler; we should refactor this in order to make it re-usable
    private String getOriginalUser(UserMessage userMessage, String type) {
        if (userMessage == null || userMessage.getMessageProperties() == null || userMessage.getMessageProperties().getProperty() == null) {
            return null;
        }
        String originalUser = null;
        for (Property property : userMessage.getMessageProperties().getProperty()) {
            if (property.getName() != null && property.getName().equals(type)) {
                originalUser = property.getValue();
                break;
            }
        }
        return originalUser;
    }


}
