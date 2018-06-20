package eu.domibus.ext.exceptions;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public enum DomibusErrorCode {

    /**
     * Generic error
     */
    DOM_001("001"),

    /**
     * Authentication error
     */
    DOM_002("002");

    private String errorCode;

    DomibusErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
