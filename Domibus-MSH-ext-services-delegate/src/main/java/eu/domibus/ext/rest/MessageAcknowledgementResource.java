package eu.domibus.ext.rest;

import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import eu.domibus.ext.services.MessageAcknowledgeService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/ext/messages/acknowledgments")
public class MessageAcknowledgementResource {

    @Autowired
    MessageAcknowledgeService messageAcknowledgeService;

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ErrorRO handleException(Exception ex) {
        return new ErrorRO(ex.getMessage());
    }

    /**
     * Acknowledges that a message has been delivered to the backend
     *
     * @param acknowledgementRequestDTO the details of the message delivered acknowledgement to be created
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    @ApiOperation(value = "Create a message delivered acknowledgement", notes = "Acknowledges that a message has been delivered to the backend",
            authorizations = @Authorization(value = "basicAuth"), tags = "acknowledgement")
    @RequestMapping(path = "/delivered", method = RequestMethod.POST)
    public MessageAcknowledgementDTO acknowledgeMessageDelivered(@RequestBody MessageAcknowledgementRequestDTO acknowledgementRequestDTO) throws MessageAcknowledgeException {
        return messageAcknowledgeService.acknowledgeMessageDelivered(acknowledgementRequestDTO.getMessageId(), acknowledgementRequestDTO.getAcknowledgeDate(), acknowledgementRequestDTO.getProperties());
    }

    /**
     * Acknowledges that a message has been processed by the backend
     *
     * @param acknowledgementRequestDTO the details of the message delivered acknowledgement to be created
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    @ApiOperation(value = "Create a message processed acknowledgement", notes = "Acknowledges that a message has been processed by the backend",
            authorizations = @Authorization(value = "basicAuth"), tags = "acknowledgement")
    @RequestMapping(path = "/processed", method = RequestMethod.POST)
    public MessageAcknowledgementDTO acknowledgeMessageProcessed(@RequestBody MessageAcknowledgementRequestDTO acknowledgementRequestDTO) throws MessageAcknowledgeException {
        return messageAcknowledgeService.acknowledgeMessageProcessed(acknowledgementRequestDTO.getMessageId(), acknowledgementRequestDTO.getAcknowledgeDate(), acknowledgementRequestDTO.getProperties());
    }

    /**
     * Gets all acknowledgments associated to a message id
     *
     * @param messageId The message id for which message acknowledgments are retrieved
     * @return All acknowledgments registered for a specific message
     */
    @ApiOperation(value = "Get acknowledgements", notes = "Gets all acknowledgments associated to a message id",
            authorizations = @Authorization(value = "basicAuth"), tags = "acknowledgement")
    @RequestMapping(path = "/{messageId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public List<MessageAcknowledgementDTO> getAcknowledgedMessages(@PathVariable(value = "messageId") String messageId) throws MessageAcknowledgeException {
        return messageAcknowledgeService.getAcknowledgedMessages(messageId);
    }


}
