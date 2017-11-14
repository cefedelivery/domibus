package eu.domibus.ext.delegate.services.usermessage;

import eu.domibus.api.message.usermessage.UserMessage;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.delegate.services.security.SecurityService;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.UserMessageException;
import eu.domibus.ext.services.UserMessageService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
@Service
public class UserMessageServiceDelegate implements UserMessageService{

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageServiceDelegate.class);

    @Autowired
    eu.domibus.api.message.UserMessageService userMessageCoreService;

    @Autowired
    DomainExtConverter domainConverter;

    @Autowired
    SecurityService securityService;

    @Override
    public UserMessageDTO getMessage(String messageId) throws UserMessageException {
        LOG.debug("Getting message with messageId='" + messageId + "'");
        securityService.checkMessageAuthorization(messageId);

        final UserMessage userMessage = userMessageCoreService.getMessage(messageId);
        if(userMessage == null) {
            return null;
        }
        return domainConverter.convert(userMessage, UserMessageDTO.class);
    }
}
