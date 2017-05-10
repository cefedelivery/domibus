package eu.domibus.web.rest;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.ext.exceptions.MessageMonitorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by musatmi on 10/05/2017.
 */
@RestController
@RequestMapping(value = "/rest/message/")
public class ResendMessagesResource {

    @Autowired
    UserMessageService userMessageService;

    @RequestMapping(path = "{messageId:.+}/restore", method = RequestMethod.PUT)
    public void restoreFailedMessage(@PathVariable(value = "messageId") String messageId) throws MessageMonitorException {
        userMessageService.restoreFailedMessage(messageId);
    }

}
