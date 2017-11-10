package eu.domibus.ext.rest;

import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.UserMessageException;
import eu.domibus.ext.services.UserMessageService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
@RestController
@RequestMapping(value = "/ext/messages/usermessages")
public class UserMessageResource {

    @Autowired
    UserMessageService userMessageService;

    @ApiOperation(value = "Get user message", notes = "Retrieve the user message with the specified message id",
            authorizations = @Authorization(value = "basicAuth"), tags = "usermessage")
    @RequestMapping(path = "/{messageId:.+}", method = RequestMethod.GET)
    public UserMessageDTO getUserMessage(@PathVariable(value = "messageId") String messageId) throws UserMessageException{
        return userMessageService.getMessage(messageId);
    }
}
