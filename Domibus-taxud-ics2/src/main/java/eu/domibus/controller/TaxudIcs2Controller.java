package eu.domibus.controller;

import eu.domibus.plugin.Submission;
import eu.domibus.taxud.MessageAccessPointSwitch;
import eu.domibus.taxud.MessageEndPointSwitch;
import eu.domibus.taxud.SubmissionLog;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
public class TaxudIcs2Controller {

    private final static Logger LOG = LoggerFactory.getLogger(TaxudIcs2Controller.class);

    private MessageAccessPointSwitch messageAccessPointSwitch;

    private MessageEndPointSwitch messageEndPointSwitch;

    private SubmissionLog submissionLog;

    @PostConstruct
    protected void init(){
        messageAccessPointSwitch=new MessageAccessPointSwitch();
        messageEndPointSwitch=new MessageEndPointSwitch();
        submissionLog=new SubmissionLog();
    }

    @PostMapping(value = "/message", consumes = "multipart/form-data")
    public void  onMessage(  @RequestPart("submissionJson") Submission submission,
                            @RequestPart(value = "payload") MultipartFile payload) {
        LOG.info("Message received:");
        submissionLog.logAccesPoints(submission);
        try {
            byte[] decode = Base64.decodeBase64(payload.getBytes());
            LOG.info("Content:[{}]",new String(decode));
        } catch (IOException e) {
            LOG.error(e.getMessage(),e);
        }
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
        submission.setAction("action");
        submission.setService("service");
        return  submission;
    }











}
