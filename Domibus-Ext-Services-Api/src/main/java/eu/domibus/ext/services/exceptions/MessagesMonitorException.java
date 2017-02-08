package eu.domibus.ext.services.exceptions;

/**
 * Specific exception to report errors occurring during Messages Monitor Service's operations.
 *
 * @author Federico Martini
 * @since 3.3
 * @see DomibusError
 */
public class MessagesMonitorException extends DomibusServiceException {

    /**
     * Constructs a new MessagesMonitorException with a specific error.
     *
     * @param domErr a DomibusError
     */
    public MessagesMonitorException(DomibusError domErr) {
        super(domErr);
    }

    /**
     * Constructs a new MessagesMonitorException with a specific error and message.
     *
     * @param domErr  a DomibusError
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     */
    public MessagesMonitorException(DomibusError domErr, String message) {
        super(domErr, message);
    }

    /**
     * Constructs a new MessagesMonitorException with a specific error, message and cause.
     *
     * @param domErr  a DomibusError
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     * @param cause   the cause of the exception.
     */
    public MessagesMonitorException(DomibusError domErr, String message, Throwable cause) {
        super(domErr, message, cause);
    }


}
