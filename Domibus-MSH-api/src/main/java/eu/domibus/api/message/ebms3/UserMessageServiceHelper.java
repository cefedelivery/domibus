package eu.domibus.api.message.ebms3;

import eu.domibus.api.message.ebms3.model.UserMessage;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageServiceHelper {

    String getOriginalSender(UserMessage userMessage);

    String getFinalRecipient(UserMessage userMessage);

    String getPartyTo(UserMessage userMessage);

    boolean isSameOriginalSender(UserMessage userMessage, String originalSender);

    boolean isSameFinalRecipient(UserMessage userMessage, String originalSender);
}
