package eu.domibus.ext.services.exceptions;

/**
 * Enum for Domibus services errors.
 *
 * @author Federico Martini
 * @since 3.3
 */
public enum DomibusError {

    /**
     * Service is not available
     */
    DOM_001(001),
    /**
     * User is not authorized to call this service
     */
    DOM_002(002),
    /**
     * Operation has failed
     */
    DOM_003(003);

    private final int errorCode;


    DomibusError(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
