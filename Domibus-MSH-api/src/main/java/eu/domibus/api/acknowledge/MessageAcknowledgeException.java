package eu.domibus.api.acknowledge;

/**
 * @author baciu
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
