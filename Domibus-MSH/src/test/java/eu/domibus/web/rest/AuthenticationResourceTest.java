package eu.domibus.web.rest;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserDetail;
import eu.domibus.common.util.WarningUtil;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import eu.domibus.security.AuthenticationService;
import eu.domibus.web.rest.ro.LoginRO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.SchedulingTaskExecutor;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RunWith(JMockit.class)
public class AuthenticationResourceTest {

    @Tested
    AuthenticationResource authenticationResource;

    @Injectable
    AuthenticationService authenticationService;

    @Injectable
    SchedulingTaskExecutor taskExecutor;

    @Injectable
    UserDomainDao userDomainDao;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    UserDomainService userDomainService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomainCoreConverter domainCoreConverter;

    @Mocked
    Logger LOG;


    @Test
    public void testWarningWhenDefaultPasswordUsed(@Mocked WarningUtil warningUtil, @Mocked final LoggerFactory loggerFactory) throws Exception {
        User user = new User("user", "user");
        LoginRO loginRO = new LoginRO();
        loginRO.setUsername("user");
        loginRO.setPassword("user");
        final UserDetail userDetail = new UserDetail(user, true);
        new Expectations() {{
            userDomainService.getDomainForUser(loginRO.getUsername());
            result = DomainService.DEFAULT_DOMAIN.getCode();

            authenticationService.authenticate("user", "user", DomainService.DEFAULT_DOMAIN.getCode());
            result = userDetail;
        }};
        authenticationResource.authenticate(loginRO, new MockHttpServletResponse());
        new Verifications() {{
            String message;
            WarningUtil.warnOutput(message = withCapture());
            assertEquals("user is using default password.", message);
            LOG.warn(withAny(""));
            times = 1;
        }};
    }

}