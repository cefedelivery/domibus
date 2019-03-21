package eu.domibus.core.crypto.spi.model;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Represent the different states that can returned by the authorization module.
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
    AUTHORIZATION_SYSTEM_DOWN,
    /**
     * Authorization module has a configuration issue.
     */
    AUTHORIZATION_MODULE_CONFIGURATION_ISSUE,
    /**
     * Auhthorization token signature is wrong.
     */
    AUTHORIZATION_TOKEN_ERROR,
    /**
     * Other unforeseen error during the authorization process.
     */
    AUTHORIZATION_OTHER
}
