package eu.domibus.ext.web.interceptor;

import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.services.AuthenticationExtService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class AuthenticationInterceptorTest {

    @Tested
    AuthenticationInterceptor authenticationInterceptor;

    @Injectable
    AuthenticationExtService authenticationExtService;

    @Test
    public void testPreHandle(@Injectable final HttpServletRequest httpRequest,
                              @Injectable final HttpServletResponse httpServletResponse,
                              @Injectable final Object handler) throws Exception {

        new Expectations() {{
            authenticationExtService.authenticate(httpRequest);
        }};

        assertTrue(authenticationInterceptor.preHandle(httpRequest, httpServletResponse, handler));
    }


    @Test
    public void testPreHandleWhenExceptionIsRaised(@Injectable final HttpServletRequest httpRequest,
                              @Injectable final HttpServletResponse httpServletResponse,
                              @Injectable final Object handler) throws Exception {

        new Expectations() {{
            authenticationExtService.authenticate(httpRequest);
            result = new AuthenticationExtException(DomibusErrorCode.DOM_002, "authentication error");
        }};

        assertFalse(authenticationInterceptor.preHandle(httpRequest, httpServletResponse, handler));
    }

}
