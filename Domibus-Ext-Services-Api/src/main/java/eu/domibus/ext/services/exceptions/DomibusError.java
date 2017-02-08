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
    DOM_001(001, "Service is not available"),
    /**
     * User is not authorized to call this service
     */
    DOM_002(002, "User is not authorized to call this service"),
    /**
     * Operation has failed
     */
    DOM_003(003, "Operation has failed");

    private final int errorCode;

    private final String errorDetail;

    DomibusError(int errorCode, String errorDetail) {

        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

}
