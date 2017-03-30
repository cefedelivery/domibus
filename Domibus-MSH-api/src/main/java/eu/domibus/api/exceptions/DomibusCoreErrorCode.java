package eu.domibus.api.exceptions;

/**
 * Enum for Domibus core errors.
 *
 * @author Federico Martini
 * @since 3.3
 */
public enum DomibusCoreErrorCode {

    /**
     * Generic error
     */
    DOM_001("001"),

    /**
     * Authentication error
     */
    DOM_002("002");

    private final String errorCode;

    DomibusCoreErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
