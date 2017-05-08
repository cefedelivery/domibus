package eu.domibus.api.message.attempt;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface MessageAttemptService {

    List<MessageAttempt> getAttemptsHistory(String messageId);

    MessageAttempt create(MessageAttempt attempt);
}
