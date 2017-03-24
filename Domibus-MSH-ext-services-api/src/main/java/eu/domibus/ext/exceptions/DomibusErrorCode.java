package eu.domibus.ext.exceptions;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public enum DomibusErrorCode {

    DOM_001("001");

    private String errorCode;

    DomibusErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
