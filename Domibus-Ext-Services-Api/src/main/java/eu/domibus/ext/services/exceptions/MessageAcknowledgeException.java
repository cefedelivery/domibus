package eu.domibus.ext.services.exceptions;

/**
 * Raised in case an exception occurs when dealing with message acknowledgments.
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public class MessageAcknowledgeException extends DomibusServiceException {

    /**
     * Constructs a new MessageAcknowledgeException with a specific error and message.
     *
     * @param domibusError  a DomibusError
     * @param message the message detail.
     */
    public MessageAcknowledgeException(DomibusError domibusError, String message) {
        super(domibusError, message);
    }

    /**
     * Constructs a new MessageAcknowledgeException with a specific error, message and cause.
     *
     * @param domibusError  a DomibusError
     * @param message the message detail.
     * @param cause   the cause of the exception.
     */
    public MessageAcknowledgeException(DomibusError domibusError, String message, Throwable cause) {
        super(domibusError, message, cause);
    }
}