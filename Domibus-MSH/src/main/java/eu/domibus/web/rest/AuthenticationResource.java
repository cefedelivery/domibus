package eu.domibus.web.rest;

import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.util.WarningUtil;
import eu.domibus.security.AuthenticationService;
import eu.domibus.web.rest.ro.LoginRO;
import eu.domibus.web.rest.ro.UserRO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    private AuthenticationService authenticationService;

    @RequestMapping(value = "authentication", method = RequestMethod.POST)
    public UserRO authenticate(@RequestBody LoginRO loginRO, HttpServletResponse response) {
        LOG.debug("Authenticating user [{}]", loginRO.getUsername());
        final UserDetail principal = authenticationService.authenticate(loginRO.getUsername(), loginRO.getPassword());
        if(principal.isDefaultPasswordUsed()){
            LOG.warn(WarningUtil.warnOutput(principal.getUsername()+" is using default password."));
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


}
