package eu.domibus.weblogic.ecas;


import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainException;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String ECAS_DOMIBUS_USER_ROLE_PREFIX = "DOMIBUS_USER_ROLE_";
    private static final String ECAS_DOMIBUS_DOMAIN_PREFIX = "DOMIBUS_DOMAIN_";

    @Autowired
    private DomainService domainService;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken) throws UsernameNotFoundException {
        UserDetails userDetails = loadUserByUsername((String) preAuthenticatedAuthenticationToken.getPrincipal());
        LOG.debug("UserDetails username={}", userDetails.getUsername());
        return userDetails;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOG.info("loadUserByUsername - start");
        if (isWeblogicSecurity()) {
            try {
                return createUserDetails(username);
            } catch (Exception ex) {
                LOG.error("error during loadUserByUserName", ex);
                throw new UsernameNotFoundException(ERR_CANNOT_RETRIEVE_USER_DETAILS, ex);
            }
        }

        throw new UsernameNotFoundException(ERR_CANNOT_FIND_USER_WITH_NAME + username);
    }

    private UserDetails createUserDetails(final String username) throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException {

        List<GrantedAuthority> userGroups = new LinkedList<>();
        List<String> userGroupsStr = new LinkedList<>();
        String domainName = null;

        //extract user role and domain
        for (Principal principal : getPrincipals()) {
            if (isUserGroupPrincipal(principal)) {
                LOG.debug(LOG_FOUND_USER_GROUP_PRINCIPAL, principal);
                if (principal.getName().startsWith(ECAS_DOMIBUS_USER_ROLE_PREFIX)) {
                    userGroupsStr.add(principal.getName().replaceAll("^" + ECAS_DOMIBUS_USER_ROLE_PREFIX, StringUtils.EMPTY));
                } else if (principal.getName().startsWith(ECAS_DOMIBUS_DOMAIN_PREFIX)) {
                    domainName = principal.getName().replaceAll("^" + ECAS_DOMIBUS_DOMAIN_PREFIX, StringUtils.EMPTY);
                }
            } else {
                if (isUserPrincipal(principal) && !username.equals(principal.getName())) {
                    LOG.error(ERR_USERNAME_DOES_NOT_MATCH_PRINCIPAL, username, principal.getName());
                    throw new AccessDeniedException(
                            String.format(EX_USERNAME_DOES_NOT_MATCH_PRINCIPAL, username, principal.getName()));
                }
            }
        }
        userGroups.add(chooseHighestUserGroup(userGroupsStr));
        UserDetail userDetail = new UserDetail(username, StringUtils.EMPTY, userGroups);
        userDetail.setDefaultPasswordUsed(false);


        setDomainFromECASGroup(domainName, userDetail);
        userDetail.setDaysTillExpiration(Integer.MAX_VALUE);

        return userDetail;
    }

    private void setDomainFromECASGroup(String domainName, UserDetail userDetail) {
        if (domibusConfigurationService.isMultiTenantAware()) {
            Domain domain = domainService.getDomains().stream().filter(d -> domainName.equalsIgnoreCase(d.getCode()))
                    .findAny()
                    .orElse(null);
            if (null == domain) {
                throw new DomainException("Could not set current domain: unknown domain (" + domainName + ")");
            }
            userDetail.setDomain(domain.getCode());
            domainContextProvider.setCurrentDomain(domain.getCode());
        } else {
            //non multi tenancy
            userDetail.setDomain(DomainService.DEFAULT_DOMAIN.getCode());
        }
    }

    private GrantedAuthority chooseHighestUserGroup(final List<String> userGroups) {
        if (userGroups.contains("AP_ADMIN")) {
            return new SimpleGrantedAuthority(AuthRole.ROLE_AP_ADMIN.name());
        } else if (userGroups.contains("ADMIN")) {
            return new SimpleGrantedAuthority(AuthRole.ROLE_ADMIN.name());
        }
        return new SimpleGrantedAuthority(AuthRole.ROLE_USER.name());
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
