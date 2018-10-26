package eu.domibus.common.model.security;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public enum UserLoginErrorReason {
    UNKNOWN,
    INACTIVE,
    SUSPENDED,
    BAD_CREDENTIALS,
    PASSWORD_EXPIRED,
}
