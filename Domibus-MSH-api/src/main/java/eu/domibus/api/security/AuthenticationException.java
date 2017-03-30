package eu.domibus.api.security;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

public class AuthenticationException extends DomibusCoreException {

    public AuthenticationException(String message) {
        this(DomibusCoreErrorCode.DOM_002, message);
    }

    public AuthenticationException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public AuthenticationException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public AuthenticationException(String message, Throwable cause) {
        this(DomibusCoreErrorCode.DOM_002, message, cause);
    }
}
