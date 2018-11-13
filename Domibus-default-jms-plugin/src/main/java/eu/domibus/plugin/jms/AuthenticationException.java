package eu.domibus.plugin.jms;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
