package eu.domibus.web.rest;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/rest/jms")
public class JmsResource {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(JmsResource.class);

    private static final String APPLICATION_JSON = "application/json";

    @Autowired
    JMSManager jmsManager;

    @RequestMapping(value = {"/destinations"}, method = GET)
    public ResponseEntity<DestinationsResponseRO> destinations() {

        final DestinationsResponseRO destinationsResponseRO = new DestinationsResponseRO();
        try {
            destinationsResponseRO.setJmsDestinations(jmsManager.getDestinations());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(destinationsResponseRO);

        } catch (RuntimeException runEx) {
            LOGGER.error("Error finding the JMS messages sources", runEx);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(destinationsResponseRO);
        }
    }

    @RequestMapping(value = {"/messages"}, method = POST)
    public ResponseEntity<MessagesResponseRO> messages(@RequestBody MessagesRequestRO request) {

        final MessagesResponseRO messagesResponseRO = new MessagesResponseRO();
        try {
            messagesResponseRO.setMessages(jmsManager.browseMessages(request.getSource(), request.getJmsType(), request.getFromDate(), request.getToDate(), request.getSelector()));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(messagesResponseRO);

        } catch (RuntimeException runEx) {
            LOGGER.error("Error browsing messages for source [" + request.getSource() + "]", runEx);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(messagesResponseRO);
        }
    }

    @RequestMapping(value = {"/messages/action"}, method = POST)
    public ResponseEntity<MessagesActionResponseRO> action(@RequestBody MessagesActionRequestRO request) {

        final MessagesActionResponseRO response = new MessagesActionResponseRO();
        response.setOutcome("Success");

        try {
            List<String> messageIds = request.getSelectedMessages();
            String[] ids = messageIds.toArray(new String[0]);

            if (request.getAction() == MessagesActionRequestRO.Action.MOVE) {
                jmsManager.moveMessages(request.getSource(), request.getDestination(), ids);
            } else if (request.getAction() == MessagesActionRequestRO.Action.REMOVE) {
                jmsManager.deleteMessages(request.getSource(), ids);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(response);

        } catch (RuntimeException runEx) {
            LOGGER.error("Error performing action [" + request.getAction() + "]", runEx);
            response.setOutcome(runEx.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(response);
        }


    }


}
