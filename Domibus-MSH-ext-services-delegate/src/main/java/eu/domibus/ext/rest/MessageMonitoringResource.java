package eu.domibus.ext.rest;

import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.ext.exceptions.MessageMonitorExtException;
import eu.domibus.ext.services.MessageMonitorExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    MessageMonitorExtService messageMonitorExtService;

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ErrorRO handleException(Exception ex) {
        return new ErrorRO(ex.getMessage());
    }

    @ApiOperation(value = "Get failed messages", notes = "Retrieve all the messages with the specified finalRecipient(if provided) that are currently in a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @RequestMapping(path = "/failed", method = RequestMethod.GET)
    public List<String> getFailedMessages(@RequestParam(value = "finalRecipient", required = false) String finalRecipient) throws MessageMonitorExtException {
        if (StringUtils.isNotEmpty(finalRecipient)) {
            return messageMonitorExtService.getFailedMessages(finalRecipient);
        } else {
            return messageMonitorExtService.getFailedMessages();
        }
    }

    @ApiOperation(value = "Get failed message elapsed time", notes = "Retrieve the time that a message has been in a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @RequestMapping(path = "/failed/{messageId:.+}/elapsedtime", method = RequestMethod.GET)
    public Long getFailedMessageInterval(@PathVariable(value = "messageId") String messageId) throws MessageMonitorExtException {
        return messageMonitorExtService.getFailedMessageInterval(messageId);
    }

    @ApiOperation(value = "Resend failed message",  notes = "Resend a message which has a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @RequestMapping(path = "/failed/{messageId:.+}/restore", method = RequestMethod.PUT)
    public void restoreFailedMessage(@PathVariable(value = "messageId") String messageId) throws MessageMonitorExtException {
        messageMonitorExtService.restoreFailedMessage(messageId);
    }

    @ApiOperation(value = "Resend all messages with SEND_FAILURE status within a certain time interval", notes = "Resend all messages with SEND_FAILURE status within a certain time interval",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @RequestMapping(path = "/failed/restore", method = RequestMethod.POST)
    public List<String> restoreFailedMessages(@RequestBody FailedMessagesCriteriaRO failedMessagesCriteriaRO) throws MessageMonitorExtException {
        return messageMonitorExtService.restoreFailedMessagesDuringPeriod(failedMessagesCriteriaRO.getFromDate(), failedMessagesCriteriaRO.getToDate());
    }

    @ApiOperation(value = "Delete failed message payload", notes = "Delete the payload of a message which has a SEND_FAILURE status",
            authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @ResponseBody
    @RequestMapping(path = "/failed/{messageId:.+}", method = RequestMethod.DELETE)
    public void deleteFailedMessage(@PathVariable(value = "messageId") String messageId) throws MessageMonitorExtException {
        messageMonitorExtService.deleteFailedMessage(messageId);
    }

    @ApiOperation(value = "Get message attempts", notes = "Retrieve the history of the delivery attempts for a certain message",
            response = MessageAttemptDTO.class, responseContainer = "List", authorizations = @Authorization(value = "basicAuth"), tags = "monitoring")
    @RequestMapping(path = "/{messageId:.+}/attempts", method = RequestMethod.GET)
    public List<MessageAttemptDTO> getMessageAttempts(@PathVariable(value = "messageId") String messageId) throws MessageMonitorExtException {
        return messageMonitorExtService.getAttemptsHistory(messageId);
    }
}
