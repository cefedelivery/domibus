package eu.domibus.weblogic.security;


import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * {@link UserDetailsService} implementation for ECAS
 *
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

    static final String ECAS_DOMIBUS_LDAP_GROUP_PREFIX_KEY = "domibus.security.ext.auth.provider.group.prefix";
    static final String ECAS_DOMIBUS_USER_ROLE_MAPPINGS_KEY = "domibus.security.ext.auth.provider.user.role.mappings";
    static final String ECAS_DOMIBUS_DOMAIN_MAPPINGS_KEY = "domibus.security.ext.auth.provider.domain.mappings";

    private static final String ECAS_DOMIBUS_MAPPING_PAIR_SEPARATOR = ";";
    private static final String ECAS_DOMIBUS_MAPPING_VALUE_SEPARATOR = "=";

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
        LOG.debug("loadUserByUsername - start");
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

    /**
     * It reads the principals (LDAP groups) returned by ECAS and create UserDetails
     *
     * @param username
     * @return UserDetails object
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     */
    protected UserDetails createUserDetails(final String username) throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException {
        LOG.debug("createUserDetails - start");
        List<GrantedAuthority> userGroups = new LinkedList<>();
        List<AuthRole> userGroupsStr = new LinkedList<>();
        String domainCode = null;
        final String ldapGroupPrefix = domibusPropertyProvider.getProperty(ECAS_DOMIBUS_LDAP_GROUP_PREFIX_KEY);
        LOG.debug("createUserDetails - LDAP group prefix is: {}", ldapGroupPrefix);

        Map<String, AuthRole> userRoleMappings = retrieveUserRoleMappings();
        Map<String, String> domainMappings = retrieveDomainMappings();

        //extract user role and domain
        for (Principal principal : getPrincipals()) {
            LOG.debug("createUserDetails - principal name: {} and class: {}", principal.getName(), principal.getClass().getName());
            if (isUserGroupPrincipal(principal)) {
                LOG.debug("Found a user group principal: {}", principal);
                final String principalName = principal.getName();

                //only Domibus mapped ldap groups
                if (principalName.startsWith(ldapGroupPrefix)) {

                    //search for user roles
                    if (userRoleMappings.get(principalName) != null) {
                        userGroupsStr.add(userRoleMappings.get(principalName));
                        LOG.debug("createUserDetails - userGroup added: {}", userRoleMappings.get(principalName));
                    } else if (domainMappings.get(principalName) != null) {
                        domainCode = domainMappings.get(principalName);
                        LOG.debug("createUserDetails - domain added: {}", domainCode);
                    }
                }
            } else {
                LOG.debug("createUserDetails - user group is not principal");
                if (isUserPrincipal(principal) && !username.equals(principal.getName())) {
                    LOG.error("Username {} does not match Principal {}", username, principal.getName());
                    throw new AccessDeniedException(
                            String.format("The provided username and the principal name do not match. username = %s, principal = %s", username, principal.getName()));
                }
            }
        }


        //chose highest privilege among LDAP user groups
        final GrantedAuthority grantedAuthority = chooseHighestUserGroup(userGroupsStr);

        Domain domain = domibusConfigurationService.isMultiTenantAware() ?
                domainService.getDomain(domainCode) : DomainService.DEFAULT_DOMAIN;

        if (null != grantedAuthority && null != domain) {
            //we set the groups only if LDAP groups are mapping on both privileges and domain code
            userGroups.add(grantedAuthority);
        }
        LOG.debug("userDetail userGroups={}", userGroups);

        UserDetail userDetail = new UserDetail(username, StringUtils.EMPTY, userGroups);
        userDetail.setDefaultPasswordUsed(false);
        userDetail.setExternalAuthProvider(true);
        if (null != domain && null != domain.getCode()) {
            userDetail.setDomain(domain.getCode());
            domainContextProvider.setCurrentDomain(domain.getCode());
        }
        LOG.debug("Domain set to: {}", domain);
        userDetail.setDaysTillExpiration(Integer.MAX_VALUE);

        LOG.debug("createUserDetails - end");
        return userDetail;
    }

    protected GrantedAuthority chooseHighestUserGroup(final List<AuthRole> userGroups) {
        SimpleGrantedAuthority simpleGrantedAuthority = null;
        if (userGroups.contains(AuthRole.ROLE_AP_ADMIN)) {
            simpleGrantedAuthority = new SimpleGrantedAuthority(AuthRole.ROLE_AP_ADMIN.name());
        } else if (userGroups.contains(AuthRole.ROLE_ADMIN)) {
            simpleGrantedAuthority = new SimpleGrantedAuthority(AuthRole.ROLE_ADMIN.name());
        } else if (userGroups.contains(AuthRole.ROLE_USER)) {
            simpleGrantedAuthority = new SimpleGrantedAuthority(AuthRole.ROLE_USER.name());
        }
        return simpleGrantedAuthority;
    }

    protected boolean isWeblogicSecurity() {
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

    protected Set<Principal> getPrincipals()
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        Subject subject = (Subject) Class.forName(WEBLOGIC_SECURITY_CLASS)
                .getMethod(WEBLOGIC_SECURITY_GET_METHOD, null)
                .invoke(null, null);
        return subject.getPrincipals();
    }

    private boolean isUserPrincipal(Principal principal) throws ClassNotFoundException {
        LOG.debug("isUserPrincipal class={}", principal.getClass().getName());
        return Class.forName(ECAS_USER).isInstance(principal);
    }

    protected boolean isUserGroupPrincipal(Principal principal) throws ClassNotFoundException {
        LOG.debug("isUserGroupPrincipal class={}", principal.getClass().getName());
        return Class.forName(ECAS_GROUP).isInstance(principal);
    }

    /**
     * @return Map of Domibus user roles and LDAP EU Login groups
     */
    protected Map<String, AuthRole> retrieveUserRoleMappings() {
        final String userRoleMappings = domibusPropertyProvider.getProperty(ECAS_DOMIBUS_USER_ROLE_MAPPINGS_KEY);
        if (StringUtils.isEmpty(userRoleMappings)) {
            throw new IllegalArgumentException("Domibus user role mappings to LDAP groups could not be empty");
        }

        return Stream.of(userRoleMappings.split(ECAS_DOMIBUS_MAPPING_PAIR_SEPARATOR))
                .map(str -> str.split(ECAS_DOMIBUS_MAPPING_VALUE_SEPARATOR))
                .collect(Collectors.toMap(str -> str[0], str -> AuthRole.valueOf(str[1])));
    }

    /**
     * @return Map of Domibus domains and LDAP EU Login groups
     */
    protected Map<String, String> retrieveDomainMappings() {
        final String domainMappings = domibusPropertyProvider.getProperty(ECAS_DOMIBUS_DOMAIN_MAPPINGS_KEY);
        if (StringUtils.isEmpty(domainMappings)) {
            throw new IllegalArgumentException("Domibus domain mappings to LDAP groups could not be empty");
        }

        return Stream.of(domainMappings.split(ECAS_DOMIBUS_MAPPING_PAIR_SEPARATOR))
                .map(str -> str.split(ECAS_DOMIBUS_MAPPING_VALUE_SEPARATOR))
                .collect(Collectors.toMap(str -> str[0], str -> str[1]));
    }
}
