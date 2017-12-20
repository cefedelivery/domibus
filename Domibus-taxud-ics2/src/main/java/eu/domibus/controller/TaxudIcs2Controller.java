package eu.domibus.controller;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
@EnableAutoConfiguration
public class TaxudIcs2Controller {

    private final static Logger LOG = LoggerFactory.getLogger(TaxudIcs2Controller.class);
    private final static String ORIGINAL_SENDER = "originalSender";
    private final static String FINAL_RECIPIENT = "finalRecipient";

    @Value("${endPoint}")
    private String endPoint;

    @RequestMapping(value = "/message", method = RequestMethod.POST, consumes = {"application/xml"}, produces = {"application/xml"})
    public void onMessage( @RequestBody Messaging message) {
        LOG.info("Message received:");
        switchAccessPoint(message);
        switchEndPoint(message);
    }

    private void switchAccessPoint(Messaging message) {
        From from = message.getUserMessage().getPartyInfo().getFrom();
        PartyId fromParty = from.getPartyId();
        String fromRole = from.getRole();

        To to = message.getUserMessage().getPartyInfo().getTo();
        PartyId toParty = to.getPartyId();
        String toRole = to.getRole();

        LOG.info("switching access point from :");
        LOG.info("From [{}] with role [{}]",fromParty,fromRole);
        LOG.info("To [{}] with role [{}]",toParty,toRole);

        from.setPartyId(toParty);
        from.setRole(toRole);

        to.setPartyId(fromParty);
        to.setRole(toRole);

        LOG.info("to:");
        LOG.info("From [{}] with role [{}]",from.getPartyId(),from.getRole());
        LOG.info("To [{}] with role [{}]",to.getPartyId(),to.getRole());
    }

    private void switchEndPoint(Messaging message) {
        List<Property> properties = message.getUserMessage().getMessageProperties().getProperty();

        Property originalSender = properties.stream()
                .filter(property -> ORIGINAL_SENDER.equals(property.getName())).reduce((a, b) -> null).get();
        String originalSenderValue = originalSender.getValue();

        Property finalRecipient = properties.stream()
                .filter(property -> FINAL_RECIPIENT.equals(property.getName())).reduce((a, b) -> null).get();
        String finalRecipientValue = finalRecipient.getValue();

        LOG.info("switching end points from:");
        LOG.info("[{}] value: [{}]",ORIGINAL_SENDER,originalSenderValue);
        LOG.info("[{}] value: [{}]",FINAL_RECIPIENT,finalRecipientValue);

        originalSender.setValue(finalRecipientValue);
        finalRecipient.setValue(originalSenderValue);

        LOG.info("[{}] value: [{}]",ORIGINAL_SENDER,originalSender.getValue());
        LOG.info("[{}] value: [{}]",FINAL_RECIPIENT,finalRecipient.getValue());
    }



    public static void main(String[] args) throws Exception {
        SpringApplication.run(TaxudIcs2Controller.class, args);
    }


}
