package eu.domibus.ext.delegate.services.security;

import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.DomibusServiceExtException;

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
     * @throws AuthenticationExtException in case the user doesn't have the permission
     * @throws DomibusServiceExtException the message doesn't exist or the finalRecipient is empty
     */
    void checkMessageAuthorization(String messageId) throws AuthenticationExtException, DomibusServiceExtException;

    /**
     * Checks it the current user has the permission to access data for the provided finalRecipient
     * @param finalRecipient
     * @throws AuthenticationExtException in case the user doesn't have the permission
     * @throws DomibusServiceExtException the message doesn't exist or the finalRecipient is empty
     */
    void checkAuthorization(String finalRecipient) throws AuthenticationExtException, DomibusServiceExtException;

    /** Returns the original user passed via the security context OR
      * null when the user has the role ROLE_ADMIN or unsecured authorizations is allowed
      * @throws AuthenticationExtException
     *
      */

    /**
     * Returns the original user passed via the security context OR
     * null when the user has the role ROLE_ADMIN or unsecured authorizations is allowed
     * @return original user passed via the security context or null
     * @throws AuthenticationExtException in case the user doesn't have the permission
     */
    String getOriginalUserFromSecurityContext() throws AuthenticationExtException;

}
