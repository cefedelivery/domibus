package eu.domibus.ebms3.security.util;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component(value = "authUtils")
public class AuthUtilsImpl implements AuthUtils {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthUtilsImpl.class);

    private static final String UNSECURE_LOGIN_ALLOWED = "domibus.auth.unsecureLoginAllowed";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    /**
     * Returns the original user passed via the security context OR
     * null value when the user has the role ROLE_ADMIN or unsecured authorization is allowed
     */
    @Override
    public String getOriginalUserFromSecurityContext() throws AuthenticationException {
        /* unsecured login allowed */
        if (isUnsecureLoginAllowed()) {
            LOG.debug("Unsecured login is allowed");
            return null;
        }

        if (SecurityContextHolder.getContext() == null || SecurityContextHolder.getContext().getAuthentication() == null) {
            LOG.error("Authentication is missing from the security context. Unsecured login is not allowed");
            throw new AuthenticationException("Authentication is missing from the security context. Unsecured login is not allowed");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String originalUser = null;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (!authorities.contains(new SimpleGrantedAuthority(AuthRole.ROLE_ADMIN.name()))) {
            originalUser = (String) authentication.getPrincipal();
            LOG.debug("User [{}] has user role and finalRecipient [{}]", getAuthenticatedUser(), originalUser);
        } else {
            LOG.debug("User [{}] has admin role", getAuthenticatedUser());
        }

        return originalUser;
    }

    @Override
    public String getAuthenticatedUser() {
        if (SecurityContextHolder.getContext() == null || SecurityContextHolder.getContext().getAuthentication() == null) {
            LOG.debug("Authentication is missing from the security context");
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @Override
    public boolean isUnsecureLoginAllowed() {
        if(domibusConfigurationService.isMultiTenantAware()) {
            LOG.debug("Unsecured login not allowed: Domibus is running in multi-tenancy mode");
            return false;
        }
        /* unsecured login allowed */
        return "true".equals(domibusPropertyProvider.getProperty(UNSECURE_LOGIN_ALLOWED, "true"));
    }

    @Override
    public boolean isSuperAdmin() {
        return checkAdminRights(AuthRole.ROLE_AP_ADMIN);
    }

    @Override
    public boolean isAdmin() {
        return checkAdminRights(AuthRole.ROLE_ADMIN);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_AP_ADMIN')")
    public void hasUserOrAdminRole() {
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AP_ADMIN')")
    public void hasAdminRole() {
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public void hasUserRole() {

    }

    @Override
    public void setAuthenticationToSecurityContext(String user, String password) {
        setAuthenticationToSecurityContext(user, password, AuthRole.ROLE_ADMIN);
    }

    @Override
    public void setAuthenticationToSecurityContext(String user, String password, AuthRole authRole) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        user,
                        password,
                        Collections.singleton(new SimpleGrantedAuthority(authRole.name()))));
    }

    protected boolean checkAdminRights(AuthRole authRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities.contains(new SimpleGrantedAuthority(authRole.name()))) {
            return true;
        }
        return false;
    }

}
