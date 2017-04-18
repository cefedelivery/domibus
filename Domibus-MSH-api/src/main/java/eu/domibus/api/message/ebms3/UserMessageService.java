package eu.domibus.api.message.ebms3;

import eu.domibus.api.message.ebms3.model.UserMessage;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageService {

    UserMessage getMessage(final String messageId);
}
