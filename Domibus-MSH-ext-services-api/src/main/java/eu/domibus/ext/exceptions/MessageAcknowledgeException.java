package eu.domibus.ext.exceptions;

/**
 * Raised in case an exception occurs when dealing with message acknowledgments.
 *
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
public class MessageAcknowledgeException extends DomibusServiceException {

    /**
     * Constructs a new instance with a specific error code and message.
     *
     * @param errorCode  a DomibusErrorCode
     * @param message the message detail.
     */
    public MessageAcknowledgeException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructs a new instance with a specific error, message and cause.
     *
     * @param errorCode  a DomibusError
     * @param message the message detail.
     * @param cause   the cause of the exception.
     */
    public MessageAcknowledgeException(DomibusErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
