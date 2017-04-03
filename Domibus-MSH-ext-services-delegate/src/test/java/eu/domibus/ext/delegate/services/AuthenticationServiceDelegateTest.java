package eu.domibus.ext.delegate.services;

import eu.domibus.api.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.acknowledge.MessageAcknowledgement;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.ext.delegate.converter.DomibusDomainConverter;
import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import eu.domibus.ext.services.AuthenticationService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class AuthenticationServiceDelegateTest {

    @Tested
    AuthenticationServiceDelegate messageAcknowledgeServiceDelegate;

    @Injectable
    eu.domibus.api.security.AuthenticationService authenticationService;

    @Test
    public void testAuthenticate(@Injectable final HttpServletRequest httpRequest) throws Exception {
        messageAcknowledgeServiceDelegate.authenticate(httpRequest);

        new Verifications() {{
            authenticationService.authenticate(httpRequest);
        }};
    }


    @Test(expected = AuthenticationException.class)
    public void testAuthenticateWhenAuthenticationExceptionIsRaised(@Injectable final HttpServletRequest httpRequest) throws Exception {
        new Expectations(messageAcknowledgeServiceDelegate) {{
            authenticationService.authenticate(httpRequest);
            result = new eu.domibus.api.security.AuthenticationException("not authenticated");
        }};

        messageAcknowledgeServiceDelegate.authenticate(httpRequest);

        new Verifications() {{
            authenticationService.authenticate(httpRequest);
        }};
    }

}
