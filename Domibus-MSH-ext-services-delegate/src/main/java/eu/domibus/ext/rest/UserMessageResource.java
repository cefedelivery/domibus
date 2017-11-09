package eu.domibus.ext.rest;

import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.UserMessageException;
import eu.domibus.ext.services.UserMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/ext/messages/usermessages")
public class UserMessageResource {

    @Autowired
    UserMessageService userMessageService;

    @RequestMapping(path = "/{messageId:.+}", method = RequestMethod.GET)
    public UserMessageDTO getUserMessage(@PathVariable(value = "messageId") String messageId) throws UserMessageException{
        return userMessageService.getMessage(messageId);
    }
}
