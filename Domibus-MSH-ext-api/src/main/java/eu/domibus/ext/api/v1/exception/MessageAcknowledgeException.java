package eu.domibus.ext.api.v1.exception;

/**
 * Raised in case an exception occurs when dealing with acknowledgment of messages
 */
public class MessageAcknowledgeException extends RuntimeException {

    public MessageAcknowledgeException() {
        super();
    }

    public MessageAcknowledgeException(String message) {
        super(message);
    }

    public MessageAcknowledgeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageAcknowledgeException(Throwable cause) {
        super(cause);
    }

    protected MessageAcknowledgeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}