package eu.domibus.core.crypto.spi.model;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Represent the different states of an authorization problem.
 */
public enum AuthorizationError {

    /**
     * The message expected values were not found.
     */
    INVALID_FORMAT,
    /**
     * Authorization rejected.
     */
    AUTHORIZATION_REJECTED,
    /**
     * Credentials used to connect to  the authorization system were rejected.
     */
    AUTHORIZATION_CONNECTION_REJECTED,
    /**
     * Authorization system not reachable.
     */
    AUTHORIZATION_SYSTEM_DOWN
}
