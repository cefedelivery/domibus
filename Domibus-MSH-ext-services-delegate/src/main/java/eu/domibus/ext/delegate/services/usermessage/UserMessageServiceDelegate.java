package eu.domibus.ext.delegate.services.usermessage;

import eu.domibus.api.message.usermessage.UserMessage;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.delegate.services.security.SecurityService;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.UserMessageException;
import eu.domibus.ext.services.UserMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserMessageServiceDelegate implements UserMessageService{

    @Autowired
    eu.domibus.api.message.UserMessageService userMessageCoreService;

    @Autowired
    DomainExtConverter domainConverter;

    @Autowired
    SecurityService securityService;

    @Override
    public UserMessageDTO getMessage(String messageId) throws UserMessageException {
        securityService.checkMessageAuthorization(messageId);

        final UserMessage userMessage = userMessageCoreService.getMessage(messageId);
        return domainConverter.convert(userMessage, UserMessageDTO.class);
    }
}
