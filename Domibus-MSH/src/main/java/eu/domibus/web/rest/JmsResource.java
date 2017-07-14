package eu.domibus.web.rest;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.web.rest.ro.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    JMSManager jmsManager;

    @RequestMapping(value = {"/destinations"}, method = GET)
    public DestinationsResponseRO destinations() {
        final DestinationsResponseRO destinationsResponseRO = new DestinationsResponseRO();
        destinationsResponseRO.setJmsDestinations(jmsManager.getDestinations());
        return destinationsResponseRO;
    }

    @RequestMapping(value = {"/messages"}, method = POST)
    public MessagesResponseRO messages(@RequestBody MessagesRequestRO request) {
        final MessagesResponseRO messagesResponseRO = new MessagesResponseRO();
        messagesResponseRO.setMessages(jmsManager.browseMessages(request.getSource(), request.getJmsType(), request.getFromDate(), request.getToDate(), request.getSelector()));
        return messagesResponseRO;
    }

    @RequestMapping(value = {"/messages/action"}, method = POST)
    public ResponseEntity<MessagesActionResponseRO> action(@RequestBody MessagesActionRequestRO request) {

        final MessagesActionResponseRO response = new MessagesActionResponseRO();
        response.setOutcome("Success");

        List<String> messageIds = request.getSelectedMessages();

        try {
            String[] ids = messageIds.toArray(new String[0]);

            if (request.getAction() == MessagesActionRequestRO.Action.MOVE) {
                jmsManager.moveMessages(request.getSource(), request.getDestination(), ids);
            } else if (request.getAction() == MessagesActionRequestRO.Action.REMOVE) {
                jmsManager.deleteMessages(request.getSource(), ids);
            }
        } catch (RuntimeException e) {
            response.setOutcome(e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.parseMediaType("application/json"))
                    .body(response);
        }


        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/json"))
                .body(response);

    }


}
