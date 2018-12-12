package eu.domibus.weblogic.ecas;


import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import javax.security.auth.Subject;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * @author Catalin Enache
 * @since 4.1
 */
@Service
public class ECASUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken>, UserDetailsService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ECASUserDetailsService.class);

    private static final String WEBLOGIC_SECURITY_SECURITY = "weblogic.security.Security";

    private static final String ERR_CANNOT_RETRIEVE_USER_DETAILS = "Cannot retrieve the user's details";

    private static final String ERR_CANNOT_FIND_USER_WITH_NAME = "Cannot find any user who has the name ";

    private static final String ERR_LOADING_WEBLOGIC_SECURITY_CLASS = "Error loading a Weblogic Security class";

    private static final String EX_USERNAME_DOES_NOT_MATCH_PRINCIPAL =
            "The provided username and the principal name do not match. username = %s, principal = %s";

    private static final String ERR_USERNAME_DOES_NOT_MATCH_PRINCIPAL = "Username {} does not match Principal {}";

    private static final String LOG_FOUND_USER_GROUP_PRINCIPAL = "Found a user group principal: {}";

    private static final String GET_CURRENT_SUBJECT = "getCurrentSubject";

    private static final String ECAS_USER = "eu.cec.digit.ecas.client.j2ee.weblogic.EcasUser";

    private static final String ECAS_GROUP = "eu.cec.digit.ecas.client.j2ee.weblogic.EcasGroup";

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken) throws UsernameNotFoundException {
        UserDetails userDetails = loadUserByUsername((String) preAuthenticatedAuthenticationToken.getPrincipal());
        LOG.debug("UserDetails username={}", userDetails.getUsername());
        return userDetails;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (isWeblogicSecurity()) {
            try {
                List<GrantedAuthority> userGroups = buildUserGroups(username);
                return createUserDetails(username, userGroups);
            } catch (Exception ex) {
                LOG.error("error during loadUserByUserName", ex);
                throw new UsernameNotFoundException(ERR_CANNOT_RETRIEVE_USER_DETAILS, ex);
            }
        }

        throw new UsernameNotFoundException(ERR_CANNOT_FIND_USER_WITH_NAME + username);
    }

    private UserDetails createUserDetails(String username, List<GrantedAuthority> userGroups) {


        return new UserDetailsBuilder().withUsername(username)
                .withAuthorities(userGroups)
                .build();
    }

    private List<GrantedAuthority> buildUserGroups(String username)
            throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException {
        List<GrantedAuthority> userGroups = new LinkedList<>();
        for (Principal principal : getPrincipals()) {
            if (isUserGroupPrincipal(principal) && principal.getName().startsWith("DOMIBUS_USER_ROLE")) {
                LOG.debug(LOG_FOUND_USER_GROUP_PRINCIPAL, principal);
                userGroups.add(new SimpleGrantedAuthority(principal.getName()));
            } else {
                if (isUserPrincipal(principal) && !username.equals(principal.getName())) {
                    LOG.error(ERR_USERNAME_DOES_NOT_MATCH_PRINCIPAL, username, principal.getName());
                    throw new AccessDeniedException(
                            String.format(EX_USERNAME_DOES_NOT_MATCH_PRINCIPAL, username, principal.getName()));
                }
            }
        }
        return userGroups;
    }

    private boolean isWeblogicSecurity() {
        boolean weblogicSecurityLoaded = false;
        try {
            Class.forName(WEBLOGIC_SECURITY_SECURITY, false, getClass().getClassLoader());
            weblogicSecurityLoaded = true;
        } catch (ClassNotFoundException e) {
            // Do nothing. It can happen when an app does not use a Weblogic security provider.
            LOG.error(ERR_LOADING_WEBLOGIC_SECURITY_CLASS, e);
        }
        return weblogicSecurityLoaded;
    }

    private Set<Principal> getPrincipals()
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        Subject subject = (Subject) Class.forName(WEBLOGIC_SECURITY_SECURITY)
                .getMethod(GET_CURRENT_SUBJECT, null)
                .invoke(null, null);
        return subject.getPrincipals();
    }

    private boolean isUserPrincipal(Principal principal) throws ClassNotFoundException {
        return Class.forName(ECAS_USER).isInstance(principal);
    }

    private boolean isUserGroupPrincipal(Principal principal) throws ClassNotFoundException {
        return Class.forName(ECAS_GROUP).isInstance(principal);
    }
}
