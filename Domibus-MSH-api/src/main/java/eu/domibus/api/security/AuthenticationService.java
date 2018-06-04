package eu.domibus.api.security;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface AuthenticationService {

    void authenticate(HttpServletRequest httpRequest) throws AuthenticationException;

    Authentication basicAuthenticate(String user, String password) throws AuthenticationException;
}
