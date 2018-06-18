package eu.domibus.ext.exceptions;

/**
 * Raised in case an exception occurs when authenticating.
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
public class AuthenticationExtException extends DomibusServiceExtException {

    public AuthenticationExtException(Throwable e) {
        this(DomibusErrorCode.DOM_002, "Authentication exception", e);
    }

    /**
     * Constructs a new instance with a specific error code and message.
     *
     * @param errorCode  a DomibusErrorCode
     * @param message the message detail.
     */
    public AuthenticationExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructs a new instance with a specific error, message and cause.
     *
     * @param errorCode  a DomibusError
     * @param message the message detail.
     * @param cause   the cause of the exception.
     */
    public AuthenticationExtException(DomibusErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
