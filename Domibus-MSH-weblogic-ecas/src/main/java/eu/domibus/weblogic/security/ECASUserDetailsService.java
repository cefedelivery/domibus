package eu.domibus.weblogic.security;


import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainException;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
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

    private static final String WEBLOGIC_SECURITY_CLASS = "weblogic.security.Security";

    private static final String WEBLOGIC_SECURITY_GET_METHOD = "getCurrentSubject";

    private static final String ECAS_USER = "eu.cec.digit.ecas.client.j2ee.weblogic.EcasUser";

    private static final String ECAS_GROUP = "eu.cec.digit.ecas.client.j2ee.weblogic.EcasGroup";

    private static final String ECAS_DOMIBUS_USER_ROLE_PREFIX_KEY = "domibus.security.ext.auth.provider.group.role.prefix";
    private static final String ECAS_DOMIBUS_DOMAIN_PREFIX_KEY = "domibus.security.ext.auth.provider.group.role.prefix";

    @Autowired
    private DomainService domainService;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

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
                throw new UsernameNotFoundException("Cannot retrieve the user's details", ex);
            }
        }

        throw new UsernameNotFoundException("Cannot find any user who has the name " + username);
    }

    private UserDetails createUserDetails(final String username) throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException {

        List<GrantedAuthority> userGroups = new LinkedList<>();
        List<String> userGroupsStr = new LinkedList<>();
        String domainName = null;
        final String userRolePrefix = domibusPropertyProvider.getDomainProperty(ECAS_DOMIBUS_USER_ROLE_PREFIX_KEY);
        final String domainPrefix = domibusPropertyProvider.getDomainProperty(ECAS_DOMIBUS_DOMAIN_PREFIX_KEY);

        //extract user role and domain
        for (Principal principal : getPrincipals()) {
            if (isUserGroupPrincipal(principal)) {
                LOG.debug("Found a user group principal: {}", principal);
                if (principal.getName().startsWith(userRolePrefix)) {
                    userGroupsStr.add(principal.getName().replaceAll("^" + userRolePrefix, StringUtils.EMPTY));
                } else if (principal.getName().startsWith(domainPrefix)) {
                    domainName = principal.getName().replaceAll("^" + domainPrefix, StringUtils.EMPTY);
                }
            } else {
                if (isUserPrincipal(principal) && !username.equals(principal.getName())) {
                    LOG.error("Username {} does not match Principal {}", username, principal.getName());
                    throw new AccessDeniedException(
                            String.format("The provided username and the principal name do not match. username = %s, principal = %s", username, principal.getName()));
                }
            }
        }
        userGroups.add(chooseHighestUserGroup(userGroupsStr));
        UserDetail userDetail = new UserDetail(username, StringUtils.EMPTY, userGroups);
        userDetail.setDefaultPasswordUsed(false);
        userDetail.setExternalAuthProvider(true);

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
            Class.forName(WEBLOGIC_SECURITY_CLASS, false, getClass().getClassLoader());
            weblogicSecurityLoaded = true;
        } catch (ClassNotFoundException e) {
            // Do nothing. It can happen when an app does not use a Weblogic security provider.
            LOG.error("Error loading a Weblogic Security class", e);
        }
        return weblogicSecurityLoaded;
    }

    private Set<Principal> getPrincipals()
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        Subject subject = (Subject) Class.forName(WEBLOGIC_SECURITY_CLASS)
                .getMethod(WEBLOGIC_SECURITY_GET_METHOD, null)
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
