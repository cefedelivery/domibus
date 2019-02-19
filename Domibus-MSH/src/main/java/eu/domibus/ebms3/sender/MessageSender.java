package eu.domibus.ebms3.sender;

import eu.domibus.ebms3.common.model.UserMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface MessageSender {

    void sendMessage(final UserMessage userMessage);
}
