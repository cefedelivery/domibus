package eu.domibus.ext.delegate.services.security;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.exceptions.DomibusServiceException;
import eu.domibus.logging.DomibusLogger;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class SecurityDefaultServiceTest {

    @Tested
    SecurityDefaultService securityDefaultService;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    AuthUtils authUtils;


    @Test(expected = DomibusServiceException.class)
    public void testCheckMessageAuthorizationWithNonExistingMessage() throws Exception {
        final String messageId = "1";
        new Expectations() {{
            userMessageService.getFinalRecipient(messageId);
            result = null;
        }};

        securityDefaultService.checkMessageAuthorization(messageId);
    }

    @Test
    public void testCheckMessageAuthorizationWithExistingMessage() throws Exception {
        final String messageId = "1";
        final String finalRecipient = "C4";
        new Expectations(securityDefaultService) {{
            userMessageService.getFinalRecipient(messageId);
            result = finalRecipient;

            securityDefaultService.checkAuthorization(finalRecipient);
        }};

        securityDefaultService.checkMessageAuthorization(messageId);
    }

    @Test
    public void testCheckAuthorizationWithAdminRole(final @Capturing DomibusLogger log) throws Exception {
        final String finalRecipient = "C4";
        new Expectations() {{
            authUtils.getOriginalUserFromSecurityContext();
            result = null;
        }};

        securityDefaultService.checkAuthorization(finalRecipient);

        new Verifications() {{
            log.debug("finalRecipient from the security context is empty, user has permission to access finalRecipient [{}]", finalRecipient);
            times = 1;
        }};
    }

    @Test(expected = AuthenticationException.class)
    public void testCheckSecurityWhenOriginalUserFromSecurityContextIsDifferent() throws Exception {
        final String finalRecipient = "C4";
        final String originalUserFromSecurityContext = "differentRecipient";

        new Expectations() {{
            authUtils.getOriginalUserFromSecurityContext();
            result = originalUserFromSecurityContext;
        }};

        securityDefaultService.checkAuthorization(finalRecipient);
    }

    @Test
    public void testCheckSecurityWhenOriginalUserFromSecurityContextIsSame() throws Exception {
        final String finalRecipient = "C4";
        final String originalUserFromSecurityContext = "C4";

        new Expectations() {{
            authUtils.getOriginalUserFromSecurityContext();
            result = originalUserFromSecurityContext;
        }};

        securityDefaultService.checkAuthorization(finalRecipient);
    }
}
