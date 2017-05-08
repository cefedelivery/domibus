package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.delegate.services.security.SecurityService;
import eu.domibus.ext.domain.MessageAttemptDTO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageMonitoringServiceDelegateTest {

    @Tested
    MessageMonitoringServiceDelegate messageMonitoringServiceDelegate;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    DomainExtConverter domibusDomainConverter;

    @Injectable
    MessageAttemptService messageAttemptService;

    @Injectable
    SecurityService securityService;

    @Test
    public void testGetFailedMessages() throws Exception {
        final String originalUserFromSecurityContext = "C4";

        new Expectations(messageMonitoringServiceDelegate) {{
            securityService.getOriginalUserFromSecurityContext();
            result = originalUserFromSecurityContext;
        }};

        messageMonitoringServiceDelegate.getFailedMessages();

        new Verifications() {{
            userMessageService.getFailedMessages(originalUserFromSecurityContext);
        }};
    }

    @Test
    public void testGetFailedMessagesForFinalRecipient() throws Exception {
        final String finalRecipient = "C4";

        messageMonitoringServiceDelegate.getFailedMessages(finalRecipient);

        new Verifications() {{
            securityService.checkAuthorization(finalRecipient);
            userMessageService.getFailedMessages(finalRecipient);
        }};
    }

    @Test
    public void testGetFailedMessageInterval() throws Exception {
        final String messageId = "1";

        messageMonitoringServiceDelegate.getFailedMessageInterval(messageId);

        new Verifications() {{
            securityService.checkMessageAuthorization(messageId);
            userMessageService.getFailedMessageElapsedTime(messageId);
        }};
    }

    @Test
    public void testRestoreFailedMessagesDuringPeriod() throws Exception {
        final String messageId = "1";
        final Date begin = new Date();
        final Date end = new Date();

        final String originalUserFromSecurityContext = "C4";

        new Expectations(messageMonitoringServiceDelegate) {{
            securityService.getOriginalUserFromSecurityContext();
            result = originalUserFromSecurityContext;
        }};

        messageMonitoringServiceDelegate.restoreFailedMessagesDuringPeriod(begin, end);

        new Verifications() {{
            userMessageService.restoreFailedMessagesDuringPeriod(begin, end, originalUserFromSecurityContext);
        }};
    }

    @Test
    public void testRestoreFailedMessage() throws Exception {
        final String messageId = "1";

        messageMonitoringServiceDelegate.restoreFailedMessage(messageId);

        new Verifications() {{
            securityService.checkMessageAuthorization(messageId);
            userMessageService.restoreFailedMessage(messageId);
        }};
    }

    @Test
    public void testDeleteFailedMessage() throws Exception {
        final String messageId = "1";

        messageMonitoringServiceDelegate.deleteFailedMessage(messageId);

        new Verifications() {{
            securityService.checkMessageAuthorization(messageId);
            userMessageService.deleteFailedMessage(messageId);
        }};
    }

    @Test
    public void testGetAttemptsHistory(@Injectable  final List<MessageAttempt> attemptsHistory) throws Exception {
        final String messageId = "1";

        new Expectations(messageMonitoringServiceDelegate) {{
            messageAttemptService.getAttemptsHistory(messageId);
            result = attemptsHistory;
        }};

        messageMonitoringServiceDelegate.getAttemptsHistory(messageId);

        new Verifications() {{
            securityService.checkMessageAuthorization(messageId);
            domibusDomainConverter.convert(attemptsHistory, MessageAttemptDTO.class);
        }};
    }
}
