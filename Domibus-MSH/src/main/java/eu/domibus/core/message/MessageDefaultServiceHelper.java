package eu.domibus.core.message;

import eu.domibus.api.message.ebms3.model.PartyId;
import eu.domibus.api.message.ebms3.model.Property;
import eu.domibus.api.message.ebms3.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.handler.DatabaseMessageHandler;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class MessageDefaultServiceHelper implements MessageServiceHelper {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabaseMessageHandler.class);

    @Override
    public String getOriginalSender(UserMessage userMessage) {
        return getOriginalUser(userMessage, MessageConstants.ORIGINAL_SENDER);
    }

    @Override
    public String getFinalRecipient(UserMessage userMessage) {
        return getOriginalUser(userMessage, MessageConstants.FINAL_RECIPIENT);
    }

    @Override
    public String getPartyTo(UserMessage userMessage) {
        //TODO check why there are multiple party ids instead of just one
        final Set<PartyId> partyId = userMessage.getPartyInfo().getTo().getPartyId();
        if (partyId == null || partyId.isEmpty()) {
            return null;
        }
        return partyId.iterator().next().getValue();
    }

    @Override
    public boolean isSameOriginalSender(UserMessage userMessage, String providedOriginalSender) {
        final String messageId = userMessage.getMessageInfo().getMessageId();
        LOG.debug("Checking for message [{}] if the provided original sender [{}] is the same as the message original sender", messageId, providedOriginalSender);

        if (StringUtils.isEmpty(providedOriginalSender)) {
            LOG.debug("Provided original user is empty");
            return false;
        }

        String messageOriginalSender = getOriginalSender(userMessage);
        if (StringUtils.equals(messageOriginalSender, providedOriginalSender)) {
            LOG.debug("For message [{}] the provided original sender [{}] is the same as the message original sender", messageId, providedOriginalSender);
            return true;
        }
        return false;
    }

    @Override
    public boolean isSameFinalRecipient(UserMessage userMessage, String providedFinalRecipient) {
        final String messageId = userMessage.getMessageInfo().getMessageId();
        LOG.debug("Checking for message [{}] if the provided final recipient [{}] is the same as the message final recipient", messageId, providedFinalRecipient);

        if (StringUtils.isEmpty(providedFinalRecipient)) {
            LOG.debug("Provided final recipient is empty");
            return false;
        }

        String messageOriginalSender = getFinalRecipient(userMessage);
        if (StringUtils.equals(messageOriginalSender, providedFinalRecipient)) {
            LOG.debug("For message [{}] the provided final recipient [{}] is the same as the message final recipient", messageId, providedFinalRecipient);
            return true;
        }
        return false;
    }

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
