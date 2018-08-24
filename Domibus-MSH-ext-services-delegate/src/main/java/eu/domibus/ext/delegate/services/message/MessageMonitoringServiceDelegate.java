package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.delegate.services.security.SecurityService;
import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.MessageMonitorExtException;
import eu.domibus.ext.services.MessageMonitorExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageMonitoringServiceDelegate implements MessageMonitorExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageMonitoringServiceDelegate.class);

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    DomainExtConverter domibusDomainConverter;

    @Autowired
    MessageAttemptService messageAttemptService;

    @Autowired
    SecurityService securityService;

    @Override
    public List<String> getFailedMessages() throws AuthenticationExtException, MessageMonitorExtException {
        String originalUserFromSecurityContext = securityService.getOriginalUserFromSecurityContext();
        return userMessageService.getFailedMessages(originalUserFromSecurityContext);
    }

    @Override
    public List<String> getFailedMessages(String finalRecipient) throws AuthenticationExtException, MessageMonitorExtException {
        LOG.debug("Getting failed messages with finalRecipient [{}]", finalRecipient);
        securityService.checkAuthorization(finalRecipient);
        return userMessageService.getFailedMessages(finalRecipient);
    }


    @Override
    public Long getFailedMessageInterval(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        securityService.checkMessageAuthorization(messageId);
        return userMessageService.getFailedMessageElapsedTime(messageId);
    }

    @Override
    public void restoreFailedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        securityService.checkMessageAuthorization(messageId);
        userMessageService.restoreFailedMessage(messageId);
    }

    @Override
    public void sendEnqueuedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        securityService.checkMessageAuthorization(messageId);
        userMessageService.sendEnqueuedMessage(messageId);
    }

    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end) throws AuthenticationExtException, MessageMonitorExtException {
        String originalUserFromSecurityContext = securityService.getOriginalUserFromSecurityContext();
        return userMessageService.restoreFailedMessagesDuringPeriod(begin, end, originalUserFromSecurityContext);
    }

    @Override
    public void deleteFailedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        securityService.checkMessageAuthorization(messageId);
        userMessageService.deleteFailedMessage(messageId);
    }

    @Override
    public List<MessageAttemptDTO> getAttemptsHistory(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        securityService.checkMessageAuthorization(messageId);
        final List<MessageAttempt> attemptsHistory = messageAttemptService.getAttemptsHistory(messageId);
        return domibusDomainConverter.convert(attemptsHistory, MessageAttemptDTO.class);
    }
}
