package eu.domibus.common.util;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
public class MessageAttemptUtil {

    public MessageAttempt saveMessageAttempt(final String messageId,
                                             final MessageAttemptStatus attemptStatus,
                                             final String attemptError,
                                             final Timestamp startDate) {
        MessageAttempt attempt = new MessageAttempt();
        attempt.setMessageId(messageId);
        attempt.setStartDate(startDate);
        attempt.setError(attemptError);
        attempt.setStatus(attemptStatus);
        attempt.setEndDate(new Timestamp(System.currentTimeMillis()));
        return attempt;
    }
}
