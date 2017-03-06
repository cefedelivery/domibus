package eu.domibus.ext.services.exceptions;

/**
 * This class is the root exception for all Domibus services.
 *
 * <p>It provides three constructors using the enum DomibusError and one constructor without any Domibus error for a more flexible usage.
 *
 * @author Federico Martini
 * @since 3.3
 * @see DomibusError
 */
public class DomibusServiceException extends RuntimeException {

    private DomibusError error;

    /**
     * Constructs a new DomibusServiceException with a specific error and message.
     *
     * @param domErr a DomibusError.
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     */
    public DomibusServiceException(DomibusError domErr, String message) {
        super("[" + domErr + "]:" + message);
        error = domErr;
    }

    /**
     * Constructs a new DomibusServiceException with a specific error, message and cause.
     *
     * @param domErr  a DomibusError.
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     * @param cause   the cause of the exception.
     */
    public DomibusServiceException(DomibusError domErr, String message, Throwable cause) {
        super("[" + domErr + "]:" + message);
        error = domErr;
    }

    public DomibusError getError() {
        return error;
    }

    public void setError(DomibusError domError) {
        this.error = domError;
    }


}
