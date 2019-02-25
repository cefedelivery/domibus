package eu.domibus.ebms3.sender;

import eu.domibus.ebms3.common.model.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class MessageSenderFactory {

    @Autowired
    UserMessageSender userMessageSender;

    @Autowired
    MessageFragmentSender messageFragmentSender;

    @Autowired
    SourceMessageSender sourceMessageSender;

    public MessageSender getMessageSender(final UserMessage userMessage) {
        if (userMessage.isSplitAndJoin()) {
            if (userMessage.getMessageFragment() != null) {
                return messageFragmentSender;
            } else {
                return sourceMessageSender;
            }
        } else {
            return userMessageSender;
        }
    }
}
