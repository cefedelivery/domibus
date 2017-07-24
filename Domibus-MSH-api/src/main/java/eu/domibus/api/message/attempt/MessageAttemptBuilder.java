package eu.domibus.api.message.attempt;

import java.sql.Timestamp;

/**
 * @author Thomas Dussart
 * @since 3.3
 */


public class MessageAttemptBuilder {

    private String messageId;
    private MessageAttemptStatus attemptStatus;
    private String attemptError;
    private Timestamp startDate;

    private MessageAttemptBuilder() {

    }

    public static MessageAttemptBuilder create() {
        return new MessageAttemptBuilder();
    }

    public MessageAttemptBuilder setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public MessageAttemptBuilder setAttemptStatus(MessageAttemptStatus attemptStatus) {
        this.attemptStatus = attemptStatus;
        return this;
    }

    public MessageAttemptBuilder setAttemptError(String attemptError) {
        this.attemptError = attemptError;
        return this;
    }

    public MessageAttemptBuilder setStartDate(Timestamp startDate) {
        this.startDate = startDate;
        return this;
    }

    public MessageAttempt build() {
        MessageAttempt attempt = new MessageAttempt();
        attempt.setMessageId(messageId);
        attempt.setStartDate(startDate);
        attempt.setError(attemptError);
        attempt.setStatus(attemptStatus);
        attempt.setEndDate(new Timestamp(System.currentTimeMillis()));
        return attempt;
    }
}
