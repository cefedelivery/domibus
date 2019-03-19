package eu.domibus.core.crypto.spi.model;

public class AuthorizationException extends RuntimeException {

    private String code;

    public AuthorizationException(final String code,final String message) {
        super(message);
        this.code=code;
    }

    public String getCode() {
        return code;
    }
}
