package eu.domibus.ext.web.interceptor;

import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.services.AuthenticationService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class AuthenticationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Autowired
    AuthenticationService authenticationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOG.debug("Intercepted request for " + request.getRequestURI());

        try {
            authenticationService.authenticate(request);
            return  true;
        } catch (AuthenticationException e) {
            response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
            return false;
        }
    }
}