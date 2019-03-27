package eu.domibus.ebms3.sender;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class RetentionListenerTest {

    @Tested
    private RetentionListener retentionListener;

    @Injectable
    private UserMessageDefaultService userMessageDefaultService;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Mocked
    private Message message;

    @Test
    public void onMessage_deletesMessage(@Mocked DomibusLogger domibusLogger) throws JMSException {
        // Given
        String messageId = "messageId";

        new Expectations() {{
            message.getStringProperty(MessageConstants.MESSAGE_ID); result = messageId;
            domibusLogger.putMDC(anyString, anyString);
        }};

        // When
        retentionListener.onMessage(message);

        // Then
        new Verifications() {{
            domainContextProvider.setCurrentDomain(anyString);
            userMessageDefaultService.deleteMessage(messageId);
        }};
    }

    @Test
    public void onMessage_addsAuthentication(@Mocked DomibusLogger domibusLogger) {
        // Given
        new Expectations() {{
            authUtils.isUnsecureLoginAllowed(); result = false;
        }};

        // When
        retentionListener.onMessage(message);

        // Then
        new Verifications() {{
            authUtils.setAuthenticationToSecurityContext(anyString, anyString, AuthRole.ROLE_ADMIN);
        }};
    }
}