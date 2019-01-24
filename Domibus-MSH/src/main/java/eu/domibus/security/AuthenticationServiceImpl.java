package eu.domibus.security;

import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.common.services.UserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation for {@link AuthenticationService}
 *
 * @author Catalin Enache
 * @since 4.1
 */
public class AuthenticationServiceImpl extends AuthenticationServiceBase implements AuthenticationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationServiceImpl.class);

    public static final String INACTIVE = "Inactive";

    public static final String SUSPENDED = "Suspended";

    @Autowired
    @Qualifier("authenticationManagerForAdminConsole")
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(noRollbackFor = AuthenticationException.class)
    public UserDetail authenticate(String username, String password, String domain) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authenticationToken);
            //expired case doesn't get handled by the handleWrongAuthentication method.
            userService.validateExpiredPassword(username);
        } catch (CredentialsExpiredException ex) {
            LOG.trace("Caught CredentialsExpiredException: [{}]", ex);
            throw ex;
        } catch (AuthenticationException ae) {
            LOG.trace("Caught AuthenticationException: [{}]", ae.getClass().getName());
            UserLoginErrorReason userLoginErrorReason = userService.handleWrongAuthentication(username);
            if (UserLoginErrorReason.INACTIVE == userLoginErrorReason) {
                throw new DisabledException(INACTIVE, ae);
            } else if (UserLoginErrorReason.SUSPENDED == userLoginErrorReason) {
                throw new LockedException(SUSPENDED, ae);
            }
            LOG.trace("AuthenticationException: {}", ae.getMessage());
            throw ae;
        }

        userService.handleCorrectAuthentication(username);
        UserDetail principal = (UserDetail) authentication.getPrincipal();
        principal.setDomain(domain);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return principal;
    }


}
