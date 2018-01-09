package eu.domibus.controller;

import eu.domibus.plugin.Submission;
import eu.domibus.taxud.SubmissionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
public class TaxudIcs2Controller {

    private final static Logger LOG = LoggerFactory.getLogger(TaxudIcs2Controller.class);

    private final static String ORIGINAL_SENDER = "originalSender";

    /*private MessageAccessPointSwitch messageAccessPointSwitch;

    private MessageEndPointSwitch messageEndPointSwitch;*/

    private SubmissionLog submissionLog;

    private CertificateLogging certificateLogging;

    private PayloadLogging payloadLogging;

    @Value("${invalid.original.sender}")
    private String invalidSender;

    @Autowired
    public TaxudIcs2Controller(CertificateLogging certificateLogging, PayloadLogging payloadLogging) {
        this.certificateLogging = certificateLogging;
        this.payloadLogging = payloadLogging;
    }

    @PostConstruct
    protected void init() {
       /* messageAccessPointSwitch = new MessageAccessPointSwitch();
        messageEndPointSwitch = new MessageEndPointSwitch();*/
        submissionLog = new SubmissionLog();
    }

    @PostMapping(value = "/message", consumes = "multipart/form-data")
    public void onMessage(@RequestPart("submissionJson") Submission submission,
                          @RequestPart(value = "payload") byte[] payload) {
        LOG.info("Message received:");
        submissionLog.logAccesPoints(submission);
        payloadLogging.log(payload);
    }

    @PostMapping(value = "/authenticate", consumes = "multipart/form-data")
    public boolean authenticate(@RequestPart("submissionJson") Submission submission,
                                @RequestPart(value = "certificate") byte[] certficiate) {
        LOG.info("Authentication required:");
        submissionLog.logAccesPoints(submission);
        Submission.TypedProperty originalSender = extractOriginalSender(submission);
        if (originalSender == null || invalidSender.equals(originalSender.getValue())) {
            return false;
        }
        certificateLogging.log(certficiate);
        return true;
    }

    private Submission.TypedProperty extractOriginalSender(@RequestPart("submissionJson") Submission submission) {
        Submission.TypedProperty originalSender = null;
        Collection<Submission.TypedProperty> properties = submission.getMessageProperties();
        for (Submission.TypedProperty property : properties) {
            if (ORIGINAL_SENDER.equals(property.getKey())) {
                originalSender = property;
                break;
            }
        }
        return originalSender;
    }


    //for testing purpose.
    @RequestMapping(value = "/message", method = RequestMethod.GET)
    public Submission onMessage() {
        Submission submission = new Submission();
        submission.setFromRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        submission.setToRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        submission.addFromParty("domibus-blue", "urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        submission.addToParty("domibus-red", "urn:oasis:names:tc:ebcore:partyid-type:unregistered");

        submission.addMessageProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:wrong#sender");
        submission.addMessageProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        submission.setAction("action");
        submission.setService("service");
        return submission;
    }

}
