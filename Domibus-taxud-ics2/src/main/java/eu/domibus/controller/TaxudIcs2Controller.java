package eu.domibus.controller;

import eu.domibus.plugin.Submission;
import eu.domibus.taxud.MessageAccessPointSwitch;
import eu.domibus.taxud.MessageEndPointSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
public class TaxudIcs2Controller {

    private final static Logger LOG = LoggerFactory.getLogger(TaxudIcs2Controller.class);

    private MessageAccessPointSwitch messageAccessPointSwitch;

    private MessageEndPointSwitch messageEndPointSwitch;

    @PostConstruct
    protected void init(){
        messageAccessPointSwitch=new MessageAccessPointSwitch();
        messageEndPointSwitch=new MessageEndPointSwitch();
    }

    @RequestMapping(value = "/message", method = RequestMethod.POST)
    public Submission onMessage( @RequestBody Submission submission) {
        LOG.info("Message received:");
        messageAccessPointSwitch.switchAccessPoint(submission);
        messageEndPointSwitch.switchEndPoint(submission);
        submission.setConversationId(submission.getMessageId());
        submission.setMessageId(UUID.randomUUID() + "@domibus.eu");
        return submission;
    }

    @RequestMapping(value = "/message", method = RequestMethod.GET)
    public Submission onMessage( ) {
        Submission submission=new Submission();
        submission.setFromRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        submission.setToRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        submission.addFromParty("domibus-blue","urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        submission.addToParty("domibus-red","urn:oasis:names:tc:ebcore:partyid-type:unregistered");

        submission.addMessageProperty("originalSender","urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1");
        submission.addMessageProperty("finalRecipient","urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        return  submission;
    }











}
