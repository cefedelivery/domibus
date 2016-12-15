package eu.domibus.logging;


import eu.domibus.logging.api.MessageCode;

/**
 * @author Cosmin Baciu
 */
public enum DomibusMessageCode implements MessageCode {

    SEC_CONNECTION_ATTEMPT("SEC-001", "The host %s attempted to access %s without any certificate");

    String code;
    String message;

    DomibusMessageCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
