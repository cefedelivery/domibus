package eu.domibus.ext.delegate.services.security;

import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.exceptions.DomibusServiceException;

/**
 * <p>Service used internally in the delegate module to check user permissions on messages</p>
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface SecurityService {

    /**
     * Checks it the current user has the permission to access the message
     * @param messageId
     * @throws AuthenticationException in case the user doesn't have the permission
     * @throws DomibusServiceException the message doesn't exist or the finalRecipient is empty
     */
    void checkMessageAuthorization(String messageId) throws AuthenticationException, DomibusServiceException;

    /**
     * Checks it the current user has the permission to access data for the provided finalRecipient
     * @param finalRecipient
     * @throws AuthenticationException in case the user doesn't have the permission
     */
    void checkAuthorization(String finalRecipient) throws AuthenticationException, DomibusServiceException;

    /** Returns the original user passed via the security context OR
      * null when the user has the role ROLE_ADMIN or unsecured authorizations is allowed
      * */
    String getOriginalUserFromSecurityContext() throws AuthenticationException;

}
