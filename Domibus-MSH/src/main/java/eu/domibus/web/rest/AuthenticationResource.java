package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.*;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.util.WarningUtil;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.security.AuthenticationService;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.rest.ro.LoginRO;
import eu.domibus.web.rest.ro.UserRO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.SchedulingTaskExecutor;
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

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationResource.class);

    @Autowired
    protected AuthenticationService authenticationService;

    @Qualifier("taskExecutor")
    @Autowired
    protected SchedulingTaskExecutor schedulingTaskExecutor;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    protected DomainCoreConverter domainCoreConverter;

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler({AuthenticationException.class})
    public ErrorRO handleException(Exception ex) {
        return new ErrorRO(ex.getMessage());
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({DomainException.class})
    public ErrorRO handleDomainException(Exception ex) {
        return new ErrorRO(ex.getMessage());
    }

    @RequestMapping(value = "authentication", method = RequestMethod.POST)
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public UserRO authenticate(@RequestBody LoginRO loginRO, HttpServletResponse response) {
        String domain = userDomainService.getDomainForUser(loginRO.getUsername());
        LOG.debug("Determined domain [{}] for user [{}]", domain, loginRO.getUsername());
        domainContextProvider.setCurrentDomain(domain);

        LOG.debug("Authenticating user [{}]", loginRO.getUsername());
        final UserDetail principal = authenticationService.authenticate(loginRO.getUsername(), loginRO.getPassword(), domain);
        if (principal.isDefaultPasswordUsed()) {
            LOG.warn(WarningUtil.warnOutput(principal.getUsername() + " is using default password."));
        }

        //Parse Granted authorities to a list of string authorities
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : principal.getAuthorities()) {
            authorities.add(grantedAuthority.getAuthority());
        }

        UserRO userRO = new UserRO();
        userRO.setUsername(loginRO.getUsername());
        userRO.setAuthorities(authorities);
        userRO.setDefaultPasswordUsed(principal.isDefaultPasswordUsed());
        return userRO;
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

    @RequestMapping(value = "user", method = RequestMethod.GET)
    public String getUser() {
        UserDetail securityUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return securityUser.getUsername();
    }

    /**
     * Retrieve the current domain of the current user (in multi-tenancy mode)
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
     */
    @RequestMapping(value = "user/domain", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setCurrentDomain(@RequestBody String domainCode) {
        LOG.debug("Setting current domain " + domainCode);
        authenticationService.changeDomain(domainCode);
    }

}
