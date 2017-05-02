package eu.domibus.ext.delegate.services.authentication;

import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.services.AuthenticationService;
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
public class AuthenticationServiceDelegate implements AuthenticationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationServiceDelegate.class);

    @Autowired
    private eu.domibus.api.security.AuthenticationService authenticationService;

    @Override
    public void authenticate(HttpServletRequest httpRequest) throws AuthenticationException {
        try {
            authenticationService.authenticate(httpRequest);
        } catch (Exception e) {
            //already logged by the authentication service
            throw new AuthenticationException(e);
        }

    }
}
