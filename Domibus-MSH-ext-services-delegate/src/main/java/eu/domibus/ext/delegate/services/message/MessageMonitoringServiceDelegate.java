package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.ext.delegate.converter.DomibusDomainConverter;
import eu.domibus.ext.delegate.services.security.SecurityService;
import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.ext.exceptions.AuthenticationException;
import eu.domibus.ext.exceptions.MessageMonitorException;
import eu.domibus.ext.services.MessageMonitorService;
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
public class MessageMonitoringServiceDelegate implements MessageMonitorService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageMonitoringServiceDelegate.class);

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    DomibusDomainConverter domibusDomainConverter;

    @Autowired
    MessageAttemptService messageAttemptService;

    @Autowired
    SecurityService securityService;

    @Override
    public List<String> getFailedMessages() throws AuthenticationException, MessageMonitorException {
        String originalUserFromSecurityContext = securityService.getOriginalUserFromSecurityContext();
        return userMessageService.getFailedMessages(originalUserFromSecurityContext);
    }

    @Override
    public List<String> getFailedMessages(String finalRecipient) throws AuthenticationException, MessageMonitorException {
        LOG.debug("Getting failed messages with finalRecipient [{}]", finalRecipient);
        securityService.checkAuthorization(finalRecipient);
        return userMessageService.getFailedMessages(finalRecipient);
    }


    @Override
    public Long getFailedMessageInterval(String messageId) throws AuthenticationException, MessageMonitorException {
        securityService.checkMessageAuthorization(messageId);
        return userMessageService.getFailedMessageElapsedTime(messageId);
    }

    @Override
    public void restoreFailedMessage(String messageId) throws AuthenticationException, MessageMonitorException {
        securityService.checkMessageAuthorization(messageId);
        userMessageService.restoreFailedMessage(messageId);
    }

    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end) throws AuthenticationException, MessageMonitorException {
        String originalUserFromSecurityContext = securityService.getOriginalUserFromSecurityContext();
        return userMessageService.restoreFailedMessagesDuringPeriod(begin, end, originalUserFromSecurityContext);
    }

    @Override
    public void deleteFailedMessage(String messageId) throws AuthenticationException, MessageMonitorException {
        securityService.checkMessageAuthorization(messageId);
        userMessageService.deleteFailedMessage(messageId);
    }

    @Override
    public List<MessageAttemptDTO> getAttemptsHistory(String messageId) throws AuthenticationException, MessageMonitorException {
        securityService.checkMessageAuthorization(messageId);
        final List<MessageAttempt> attemptsHistory = messageAttemptService.getAttemptsHistory(messageId);
        return domibusDomainConverter.convert(attemptsHistory, MessageAttemptDTO.class);
    }
}
