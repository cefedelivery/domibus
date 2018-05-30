package eu.domibus.security;

import eu.domibus.api.multitenancy.DomainException;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.common.services.UserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private DomainService domainService;

    @Transactional(noRollbackFor = AuthenticationException.class)
    public UserDetail authenticate(String username, String password, String domain) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(authenticationToken);
            userService.handleCorrectAuthentication(username);
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
        
        final UserDetail principal = (UserDetail) authentication.getPrincipal();
        principal.setDomain(domain);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return principal;
    }


    /**
     * Set the domain in the current security context
     */
    public void changeDomain(String domainCode) {

        if (StringUtils.isEmpty(domainCode)) {
            throw new DomainException("Could not set current domain: domain is empty");
        }
        if (!domainService.getDomains().stream().anyMatch(d -> domainCode.equalsIgnoreCase(d.getCode()))) {
            throw new DomainException("Could not set current domain: unknown domain (" + domainCode + ")");
        }

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetail securityUser = (UserDetail) authentication.getPrincipal();
        securityUser.setDomain(domainCode);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


}
