package eu.domibus.ext.web.interceptor;

import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.services.AuthenticationExtService;
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
    AuthenticationExtService authenticationExtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOG.debug("Intercepted request for " + request.getRequestURI());

        try {
            authenticationExtService.authenticate(request);
            return  true;
        } catch (AuthenticationExtException e) {
            response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
            return false;
        }
    }
}