package eu.domibus.web.rest;

import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.util.WarningUtil;
import eu.domibus.security.AuthenticationService;
import eu.domibus.web.rest.ro.LoginRO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RunWith(JMockit.class)
public class AuthenticationResourceTest {
    @Tested AuthenticationResource authenticationResource;
    @Injectable AuthenticationService authenticationService;
    @Mocked
    Logger LOG;
    @Test
    public void testWarningWhenDefaultPasswordUsed(@Mocked WarningUtil warningUtil, @Mocked final LoggerFactory loggerFactory) throws Exception {
        User user = new User("user", "user");
        user.setPassword("user");
        LoginRO loginRO = new LoginRO();
        loginRO.setUsername("user");
        loginRO.setPassword("user");
        final UserDetail userDetail=new UserDetail(user,true);
        new Expectations(){{
            authenticationService.authenticate("user","user"); result=userDetail;
        }};
        authenticationResource.authenticate(loginRO,new MockHttpServletResponse());
        new Verifications(){{
            String message;
            WarningUtil.warnOutput(message = withCapture());
            assertEquals("user is using default password.",message);
            LOG.warn(withAny(""));times=1;
        }};
    }

}