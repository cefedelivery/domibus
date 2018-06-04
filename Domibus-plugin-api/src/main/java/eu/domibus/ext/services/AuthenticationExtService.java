package eu.domibus.ext.services;

import eu.domibus.ext.exceptions.AuthenticationExtException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Cosmin Baciu
 * @since 1.0
 */
public interface AuthenticationExtService {

    void authenticate(HttpServletRequest httpRequest) throws AuthenticationExtException;

    void basicAuthenticate(String username, String password) throws AuthenticationExtException;
}
