package eu.domibus.ext.rest;

import eu.domibus.ext.exceptions.MessageMonitorException;
import eu.domibus.ext.services.MessageMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/ext/monitoring/messages")
public class MessageMonitoringResource {

    @Autowired
    MessageMonitorService messageMonitorService;

    @RequestMapping(path = "/failed", method = RequestMethod.GET)
    public List<String> getFailedMessages(@RequestParam(value = "finalRecipient", required = false) String finalRecipient) throws MessageMonitorException {
        return messageMonitorService.getFailedMessages(finalRecipient);
    }
}
