package eu.domibus.web.security;

import eu.domibus.common.model.security.UserDetail;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class DefaultPasswordInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(DefaultPasswordInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOG.debug("Intercepted request for [{}]", request.getRequestURI());
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && (authentication.getPrincipal() instanceof UserDetail)) {
            UserDetail securityUser = (UserDetail) authentication.getPrincipal();
            if (securityUser.isDefaultPasswordUsed()) {
                response.setHeader(HttpHeaders.CONNECTION, "close");
                response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
                return false;
            }
        }
        return true;
    }
}