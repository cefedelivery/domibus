package eu.domibus.ebms3.security.util;

import eu.domibus.api.security.AuthRole;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

@Component(value = "authUtils")
public class AuthUtils {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthUtils.class);

    private static final String UNSECURE_LOGIN_ALLOWED = "domibus.auth.unsecureLoginAllowed";

    @Resource(name = "domibusProperties")
    private Properties domibusProperties;

    /* Returns the original user passed via the security context OR
    * null when the user has the role ROLE_ADMIN or unsecure authorizations is allowed
    * */
    public String getOriginalUserFromSecurityContext(SecurityContext securityContext) throws AccessDeniedException {

        /* unsecured login allowed */
        if(isUnsecureLoginAllowed()) {
            return null;
        }

        if(SecurityContextHolder.getContext() == null || SecurityContextHolder.getContext().getAuthentication() == null) {
            LOG.error("Authentication is missing from the security context. Unsecure login is not allowed");
            throw new AccessDeniedException("Authentication is missing from the security context. Unsecure login is not allowed");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String originalUser = null;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if(!authorities.contains(new SimpleGrantedAuthority(AuthRole.ROLE_ADMIN.name()))) {
            originalUser = (String) authentication.getPrincipal();
            LOG.debug("Security context OriginalUser is " + originalUser);
        }

        return originalUser;
    }

    public boolean isUnsecureLoginAllowed() {
        /* unsecured login allowed */
        return "true".equals(domibusProperties.getProperty(UNSECURE_LOGIN_ALLOWED, "true"));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void hasUserOrAdminRole() {}

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void hasAdminRole() {}

    public void setAuthenticationToSecurityContext(String user, String password) {
        setAuthenticationToSecurityContext(user, password, AuthRole.ROLE_ADMIN);
    }

    public void setAuthenticationToSecurityContext(String user, String password, AuthRole authRole) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        user,
                        password,
                        Collections.singleton(new SimpleGrantedAuthority(authRole.name()))));
    }

}
