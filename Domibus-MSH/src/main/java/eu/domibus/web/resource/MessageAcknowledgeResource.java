package eu.domibus.web.resource;

import eu.domibus.service.acknowledge.MessageAcknowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author baciu
 */
@RestController
@RequestMapping("message/acknowledge")
public class MessageAcknowledgeResource {

    //the CustomAuthenticationInterceptor will be applied to this REST resource and it will perform the authentication

    @Autowired
    MessageAcknowledgeService messageAcknowledgeService;

    @RequestMapping(value = "/{messageId}", method = RequestMethod.PUT, produces = "application/json")
    public void acknowledgeMessage(@PathVariable String messageId) {
        messageAcknowledgeService.acknowledgeMessage(messageId);
    }
}
