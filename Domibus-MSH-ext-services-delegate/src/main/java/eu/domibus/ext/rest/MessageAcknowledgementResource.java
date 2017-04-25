package eu.domibus.ext.rest;

import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import eu.domibus.ext.services.MessageAcknowledgeService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Registers a message delivered acknowledgment for a specific message using the provided properties
     *
     * @param messageAcknowledgementDTO the details of the message delivered acknowledgement to be created
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    @RequestMapping(path = "/delivered", method = RequestMethod.POST)
    public MessageAcknowledgementDTO acknowledgeMessageDelivered(@RequestBody MessageAcknowledgementDTO messageAcknowledgementDTO) throws MessageAcknowledgeException {
        return messageAcknowledgeService.acknowledgeMessageDelivered(messageAcknowledgementDTO.getMessageId(), messageAcknowledgementDTO.getAcknowledgeDate(), messageAcknowledgementDTO.getProperties());
    }

    /**
     * Registers a message delivered acknowledgment for a specific message using the provided properties
     *
     * @param messageAcknowledgementDTO the details of the message delivered acknowledgement to be created
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    @RequestMapping(path = "/processed", method = RequestMethod.POST)
    public MessageAcknowledgementDTO acknowledgeMessageProcessed(@RequestBody MessageAcknowledgementDTO messageAcknowledgementDTO) throws MessageAcknowledgeException {
        return messageAcknowledgeService.acknowledgeMessageProcessed(messageAcknowledgementDTO.getMessageId(), messageAcknowledgementDTO.getAcknowledgeDate(), messageAcknowledgementDTO.getProperties());
    }

    /**
     * Gets all acknowledgments associated to a message id
     *
     * @param messageId The message id for which message acknowledgments are retrieved
     * @return All acknowledgments registered for a specific message
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<MessageAcknowledgementDTO> getAcknowledgedMessages(@RequestParam(value = "messageId") String messageId) throws MessageAcknowledgeException {
        return messageAcknowledgeService.getAcknowledgedMessages(messageId);
    }


}
