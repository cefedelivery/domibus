package eu.domibus.ext.rest;

import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.ext.exceptions.MessageMonitorException;
import eu.domibus.ext.services.MessageMonitorService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/ext/monitoring/messages")
public class MessageMonitoringResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageMonitoringResource.class);

    @Autowired
    MessageMonitorService messageMonitorService;

    @ApiOperation(value = "Get failed messages", notes = "Retrieve all the messages with the specified finalRecipient(if provided) that are currently in a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @RequestMapping(path = "/failed", method = RequestMethod.GET)
    public List<String> getFailedMessages(@RequestParam(value = "finalRecipient", required = false) String finalRecipient) throws MessageMonitorException {
        if (StringUtils.isNotEmpty(finalRecipient)) {
            return messageMonitorService.getFailedMessages(finalRecipient);
        } else {
            return messageMonitorService.getFailedMessages();
        }
    }

    @ApiOperation(value = "Get failed message elapsed time", notes = "Retrieve the time that a message has been in a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @RequestMapping(path = "/failed/{messageId:.+}/elapsedtime", method = RequestMethod.GET)
    public Long getFailedMessageInterval(@PathVariable(value = "messageId") String messageId) throws MessageMonitorException {
        return messageMonitorService.getFailedMessageInterval(messageId);
    }

    @ApiOperation(value = "Resend failed message",  notes = "Resend a message which has a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @RequestMapping(path = "/failed/{messageId:.+}/restore", method = RequestMethod.PUT)
    public void restoreFailedMessage(@PathVariable(value = "messageId") String messageId) throws MessageMonitorException {
        messageMonitorService.restoreFailedMessage(messageId);
    }

    @ApiOperation(value = "Resend all messages with SEND_FAILURE status within a certain time interval", notes = "Resend all messages with SEND_FAILURE status within a certain time interval",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @RequestMapping(path = "/failed/restore", method = RequestMethod.POST)
    public List<String> restoreFailedMessages(@RequestBody FailedMessagesCriteriaRO failedMessagesCriteriaRO) throws MessageMonitorException {
        return messageMonitorService.restoreFailedMessagesDuringPeriod(failedMessagesCriteriaRO.getFromDate(), failedMessagesCriteriaRO.getToDate());
    }

    @ApiOperation(value = "Delete failed message payload", notes = "Delete the payload of a message which has a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @ResponseBody
    @RequestMapping(path = "/failed/{messageId:.+}", method = RequestMethod.DELETE)
    public void deleteFailedMessage(@PathVariable(value = "messageId") String messageId) throws MessageMonitorException {
        messageMonitorService.deleteFailedMessage(messageId);
    }

    @ApiOperation(value = "Get message attempts", notes = "Retrieve the history of the delivery attempts for a certain message",
            response = MessageAttemptDTO.class, responseContainer = "List", authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @RequestMapping(path = "/{messageId:.+}/attempts", method = RequestMethod.GET)
    public List<MessageAttemptDTO> getMessageAttempts(@PathVariable(value = "messageId") String messageId) throws MessageMonitorException {
        return messageMonitorService.getAttemptsHistory(messageId);
    }
}
