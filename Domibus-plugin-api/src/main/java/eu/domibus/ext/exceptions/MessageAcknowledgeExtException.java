package eu.domibus.ext.exceptions;

/**
 * Raised in case an exception occurs when dealing with message acknowledgments.
 *
 * @author Tiago Miguel, Cosmin Baciu
 * @since 4.0
 */
public class MessageAcknowledgeExtException extends DomibusServiceExtException {

    /**
     * Constructs a new instance with a specific error code and message.
     *
     * @param errorCode  a DomibusErrorCode
     * @param message the message detail.
     */
    public MessageAcknowledgeExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }


    /**
     * Constructs a new instance with a specific cause.
     *
     * @param cause the cause of the exception.
     */
    public MessageAcknowledgeExtException(Throwable cause) {
        super(DomibusErrorCode.DOM_001, cause.getMessage(), cause);
    }

    /**
     * Constructs a new instance with a specific error, message and cause.
     *
     * @param errorCode  a DomibusError
     * @param message the message detail.
     * @param cause   the cause of the exception.
     */
    public MessageAcknowledgeExtException(DomibusErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * Constructs a new instance with a specific error, message and cause.
     *
     * @param message the message detail.
     * @param cause   the cause of the exception.
     */
    public MessageAcknowledgeExtException(String message, Throwable cause) {
        super(DomibusErrorCode.DOM_001, message, cause);
    }
}
