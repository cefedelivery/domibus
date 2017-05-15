package eu.domibus.web.rest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.util.JsonUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.DestinationsResponseRO;
import eu.domibus.web.rest.ro.MessagesRequestRO;
import eu.domibus.web.rest.ro.MessagesResponseRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/rest/jms")
public class JmsResource {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(JmsResource.class);

    @Autowired
    JMSManager jmsManager;

    @Autowired
    JsonUtil jsonUtil;

    @RequestMapping(value = {"/messages"}, method = POST)
    public MessagesResponseRO messages(@RequestBody MessagesRequestRO request) {
        final MessagesResponseRO messagesResponseRO = new MessagesResponseRO();
        messagesResponseRO.setMessages(jmsManager.browseMessages(request.getSource(), request.getJmsType(), request.getFromDate(), request.getToDate(), request.getSelector()));
        return messagesResponseRO;
    }

    @RequestMapping(value = {"/destinations"}, method = GET)
    public DestinationsResponseRO destinations() {
        final DestinationsResponseRO destinationsResponseRO = new DestinationsResponseRO();
        destinationsResponseRO.setJmsDestinations(jmsManager.getDestinations());
        return destinationsResponseRO;
    }

    public static class CustomJsonDateDeserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser jsonparser,
                                DeserializationContext deserializationcontext) throws IOException {

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = jsonparser.getText();
            try {
                return format.parse(date);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }

    }

}
