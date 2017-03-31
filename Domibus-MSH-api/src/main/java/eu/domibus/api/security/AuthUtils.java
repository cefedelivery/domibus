package eu.domibus.api.security;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface AuthUtils {
    /* Returns the original user passed via the security context OR
        * null when the user has the role ROLE_ADMIN or unsecure authorizations is allowed
        * */
    String getOriginalUserFromSecurityContext() throws AuthenticationException;

    String getAuthenticatedUser();

    boolean isUnsecureLoginAllowed();

    void hasUserOrAdminRole();

    void hasAdminRole();

    void setAuthenticationToSecurityContext(String user, String password);

    void setAuthenticationToSecurityContext(String user, String password, AuthRole authRole);
}
