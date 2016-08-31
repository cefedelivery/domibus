package eu.domibus.ebms3.security.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Properties;

@Component(value = "authUtils")
public class AuthUtils {

    private static final Log LOG = LogFactory.getLog(AuthUtils.class);

    private static final String UNSECURE_LOGIN_ALLOWED = "domibus.auth.unsecureLoginAllowed";

    @Resource(name = "domibusProperties")
    private Properties domibusProperties;

    /* Returns the original user passed via the security context OR
    * null when the user has the role ROLE_ADMIN or unsecure authorizations is allowed
    * */
    public String getOriginalUserFromSecurityContext(SecurityContext securityContext) {
        String originalUser = null;

        /* unsecured login allowed */
        if("true".equals(domibusProperties.getProperty(UNSECURE_LOGIN_ALLOWED, "true")))
            return originalUser;

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if(!authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                originalUser = (String) authentication.getPrincipal();
            }
        } catch (NullPointerException e) {
            LOG.info("Could not extract originalUser from authentication object, possibly due to unsecured login");
        }
        return originalUser;
    }

    public boolean isUnsecureLoginAllowed() {
        /* unsecured login allowed */
        return "true".equals(domibusProperties.getProperty("domibus.auth.unsecureLoginAllowed", "true"));
    }

}
