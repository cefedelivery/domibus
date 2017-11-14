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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationService.class);
    public static final String INACTIVE = "Inactive";
    public static final String SUSPENDED = "Suspended";

    @Autowired
    @Qualifier("authenticationManagerForAdminConsole")
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Transactional(noRollbackFor = AuthenticationException.class)
    public UserDetail authenticate(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException ae) {
            UserLoginErrorReason userLoginErrorReason = userService.handleWrongAuthentication(username);
            if(UserLoginErrorReason.INACTIVE.equals(userLoginErrorReason)){
                throw new DisabledException(INACTIVE);
            }
            else if(UserLoginErrorReason.SUSPENDED.equals(userLoginErrorReason)){
                throw new LockedException(SUSPENDED);
            }
            throw ae;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final UserDetail principal = (UserDetail) authentication.getPrincipal();
        return principal;
    }


}
