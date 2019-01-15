package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.services.UserService;
import eu.domibus.common.util.WarningUtil;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.security.AuthenticationService;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ChangePasswordRO;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.rest.ro.LoginRO;
import eu.domibus.web.rest.ro.UserRO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/security")
public class AuthenticationResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationResource.class);

    @Autowired
    protected AuthenticationService authenticationService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    protected DomainCoreConverter domainCoreConverter;

    @Autowired
    protected ErrorHandlerService errorHandlerService;

    @Autowired
    @Lazy
    @Qualifier("superUserManagementService")
    private UserService superUserManagementService;

    @Autowired
    @Lazy
    @Qualifier("userManagementService")
    private UserService userManagementService;

    @Autowired
    private AuthUtils authUtils;

    @ExceptionHandler({AccountStatusException.class})
    public ResponseEntity<ErrorRO> handleAccountStatusException(AccountStatusException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ErrorRO> handleAuthenticationException(AuthenticationException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "authentication", method = RequestMethod.POST)
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public UserRO authenticate(@RequestBody LoginRO loginRO, HttpServletResponse response) {

        String domainCode = userDomainService.getDomainForUser(loginRO.getUsername());
        LOG.debug("Determined domain [{}] for user [{}]", domainCode, loginRO.getUsername());

        if (StringUtils.isNotBlank(domainCode)) {   //domain user
            domainContextProvider.setCurrentDomain(domainCode);
        } else {                    //ap user
            domainContextProvider.clearCurrentDomain();
            domainCode = userDomainService.getPreferredDomainForUser(loginRO.getUsername());
            if (StringUtils.isBlank(domainCode)) {
                LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_UNKNOWN_USER, loginRO.getUsername());
                throw new BadCredentialsException("The username/password combination you provided are not valid. Please try again or contact your administrator.");
            }

            LOG.debug("Determined preferred domain [{}] for user [{}]", domainCode, loginRO.getUsername());
        }

        LOG.debug("Authenticating user [{}]", loginRO.getUsername());
        final UserDetail principal = authenticationService.authenticate(loginRO.getUsername(), loginRO.getPassword(), domainCode);
        if (principal.isDefaultPasswordUsed()) {
            LOG.warn(WarningUtil.warnOutput(principal.getUsername() + " is using default password."));
        }

        return createUserRO(principal, loginRO.getUsername());
    }


    @RequestMapping(value = "authentication", method = RequestMethod.DELETE)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            LOG.debug("Cannot perform logout: no user is authenticated");
            return;
        }

        LOG.debug("Logging out user [" + auth.getName() + "]");
        new CookieClearingLogoutHandler("JSESSIONID", "XSRF-TOKEN").logout(request, response, null);
        LOG.debug("Cleared cookies");
        new SecurityContextLogoutHandler().logout(request, response, auth);
        LOG.debug("Logged out");
    }

    @RequestMapping(value = "username", method = RequestMethod.GET)
    public String getUsername() {
        return getLoggedUser().getUsername();
    }

    @RequestMapping(value = "user", method = RequestMethod.GET)
    public UserRO getUser() {
        LOG.info("get user - start");
        UserDetail userDetail = getLoggedUser();

        return createUserRO(userDetail, userDetail.getUsername());
    }


    /**
     * Retrieve the current domain of the current user (in multi-tenancy mode)
     *
     * @return the current domain
     */
    @RequestMapping(value = "user/domain", method = RequestMethod.GET)
    public DomainRO getCurrentDomain() {
        LOG.debug("Getting current domain");
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        return domainCoreConverter.convert(domain, DomainRO.class);
    }

    /**
     * Set the current domain of the current user (in multi-tenancy mode)
     *
     * @param domainCode the code of the new current domain
     */
    @RequestMapping(value = "user/domain", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setCurrentDomain(@RequestBody String domainCode) {
        LOG.debug("Setting current domain " + domainCode);
        authenticationService.changeDomain(domainCode);
    }

    /**
     * Set the password of the current user
     *
     * @param param the object holding the current and new passwords of the current user
     *
     * */
    @RequestMapping(value = "user/password", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody ChangePasswordRO param) {
        UserDetail loggedUser = this.getLoggedUser();
        LOG.debug("Changing password for user [{}]", loggedUser.getUsername());
        getUserService().changePassword(loggedUser.getUsername(), param.getCurrentPassword(), param.getNewPassword());
        loggedUser.setDefaultPasswordUsed(false);
    }

    UserDetail getLoggedUser() {
        return  (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    UserService getUserService() {
        if (authUtils.isSuperAdmin()) {
            return superUserManagementService;
        } else {
            return userManagementService;
        }
    }


    private UserRO createUserRO(UserDetail principal, String username) {
        //Parse Granted authorities to a list of string authorities
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : principal.getAuthorities()) {
            authorities.add(grantedAuthority.getAuthority());
        }

        UserRO userRO = new UserRO();
        userRO.setUsername(username);
        userRO.setAuthorities(authorities);
        userRO.setDefaultPasswordUsed(principal.isDefaultPasswordUsed());
        userRO.setDaysTillExpiration(principal.getDaysTillExpiration());
        userRO.setExternalAuthProvider(principal.isExternalAuthProvider());
        return userRO;
    }

}
