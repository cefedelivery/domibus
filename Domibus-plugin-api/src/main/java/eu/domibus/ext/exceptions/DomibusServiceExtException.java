package eu.domibus.ext.exceptions;

/**
 * This class is the root exception for all Domibus services.
 *
 * <p>It provides three constructors using the enum DomibusError and one constructor without any Domibus error for a more flexible usage.
 *
 * @author  Tiago Miguel, Cosmin Baciu
 * @since 4.0
 */
public class DomibusServiceExtException extends RuntimeException {

    private DomibusErrorCode errorCode;



    /**
     * Constructs a new instance with a specific error code and message.
     *
     * @param errorCode  a DomibusErrorCode
     * @param message the message detail.
     */
    public DomibusServiceExtException(DomibusErrorCode errorCode, String message) {
        super("[" + errorCode + "]:" + message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new instance with a specific error, message and cause.
     *
     * @param errorCode a DomibusErrorCode
     * @param message the message detail.
     * @param throwable the cause of the exception.
     */
    public DomibusServiceExtException(DomibusErrorCode errorCode, String message, Throwable throwable) {
        super("[" + errorCode + "]:" + message, throwable);
        this.errorCode = errorCode;
    }

    public DomibusErrorCode getErrorCode() {
        return errorCode;
    }
}
