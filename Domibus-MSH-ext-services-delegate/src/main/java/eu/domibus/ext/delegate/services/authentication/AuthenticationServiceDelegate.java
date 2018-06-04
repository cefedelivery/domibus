package eu.domibus.ext.delegate.services.authentication;

import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class AuthenticationServiceDelegate implements AuthenticationExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationServiceDelegate.class);

    @Autowired
    private eu.domibus.api.security.AuthenticationService authenticationService;

    @Override
    public void authenticate(HttpServletRequest httpRequest) throws AuthenticationExtException {
        try {
            authenticationService.authenticate(httpRequest);
        } catch (Exception e) {
            //already logged by the authentication service
            throw new AuthenticationExtException(e);
        }

    }

    @Override
    public void basicAuthenticate(String user, String password) throws AuthenticationExtException {
        try {
            authenticationService.basicAuthenticate(user, password);
        } catch (Exception e) {
            //already logged by the authentication service
            throw new AuthenticationExtException(e);
        }
    }
}
