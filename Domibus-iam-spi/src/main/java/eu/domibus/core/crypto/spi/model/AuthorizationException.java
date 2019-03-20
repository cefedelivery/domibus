package eu.domibus.core.crypto.spi.model;

/**
 * Exception thrown by the new Authorization module.
 */
public class AuthorizationException extends RuntimeException {

    private final String code;

    private AuthorizationError authorizationError;

    public AuthorizationException(final String httpCode, final String message) {
        super(message);
        this.code = httpCode;
    }

    public AuthorizationException(final String httpCode, final String message, final Throwable cause) {
        super(message, cause);
        this.code = httpCode;
    }

    public AuthorizationException(final AuthorizationError authorizationError, final String httpCode, final String message) {
        this(httpCode, message);
        this.authorizationError = authorizationError;
    }

    public String getCode() {
        return code;
    }

    public AuthorizationError getAuthorizationError() {
        return authorizationError;
    }
}
