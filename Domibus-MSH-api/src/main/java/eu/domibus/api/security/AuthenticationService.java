package eu.domibus.api.security;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface AuthenticationService {

    void authenticate(HttpServletRequest httpRequest) throws AuthenticationException;
}
