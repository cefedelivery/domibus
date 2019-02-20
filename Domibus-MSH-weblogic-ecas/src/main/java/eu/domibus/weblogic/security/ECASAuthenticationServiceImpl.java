package eu.domibus.weblogic.security;

import eu.domibus.common.model.security.UserDetail;
import eu.domibus.security.AuthenticationService;
import eu.domibus.security.AuthenticationServiceBase;

/**
 * Implementation for ECAS of {@link AuthenticationService}
 * Nothing to do here
 *
 * @author Catalin Enache
 * @since 4.1
 */
public class ECASAuthenticationServiceImpl extends AuthenticationServiceBase implements AuthenticationService {

    @Override
    public UserDetail authenticate(String username, String password, String domain) {
        return null;
    }
}
