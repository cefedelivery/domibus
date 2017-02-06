package eu.domibus.ext.services.exceptions;

/**
 * Root exception for Domibus services.
 *
 * @author Federico Martini
 * @since 3.3
 */
public class DomibusServiceException extends RuntimeException {

    private DomibusError domibusError;

    /**
     * Constructs a new DomibusServiceException with a specific error.
     *
     * @param domErr a Domibus error.
     */
    public DomibusServiceException(DomibusError domErr) {
        super();
        domibusError = domErr;
    }

    /**
     * Constructs a new DomibusServiceException with a specific error and message.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public DomibusServiceException(DomibusError domErr, String message) {
        super(message);
        domibusError = domErr;
    }

    public DomibusError getDomibusError() {
        return domibusError;
    }

    public void setDomibusError(DomibusError domibusError) {
        this.domibusError = domibusError;
    }


}
