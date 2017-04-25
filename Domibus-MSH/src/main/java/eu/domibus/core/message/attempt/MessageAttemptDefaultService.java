package eu.domibus.core.message.attempt;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageAttemptDefaultService implements MessageAttemptService {

    @Override
    public List<MessageAttempt> getAttemptsHistory(String messageId) {
        return null;
    }
}
