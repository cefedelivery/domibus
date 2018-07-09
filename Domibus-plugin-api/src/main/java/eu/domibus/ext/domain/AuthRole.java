package eu.domibus.ext.domain;

/**
 * Supported Domibus roles. Use these roles in case custom authentication is implemented
 */
public class AuthRole {

    private AuthRole() {
    }

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

}