package eu.domibus.controller;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import eu.domibus.taxud.CertificateLogging;
import eu.domibus.taxud.PayloadLogging;
import eu.domibus.taxud.Umds;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
public class UmdsController {

    private final static Logger LOG = LoggerFactory.getLogger(UmdsController.class);



    private CertificateLogging certificateLogging;

    private PayloadLogging payloadLogging;

    @Value("${invalid.original.user.identifier}")
    private String invalidSender;

    public final MetricRegistry metricRegistry;


    @Autowired
    public UmdsController(CertificateLogging certificateLogging, PayloadLogging payloadLogging,MetricRegistry metricRegistry) {
        this.certificateLogging = certificateLogging;
        this.payloadLogging = payloadLogging;
        this.metricRegistry=metricRegistry;
    }


    @RequestMapping(method = RequestMethod.POST,value = "/authenticate",produces="application/json")
    public boolean authenticate(@RequestBody Umds submission) {
        final Timer.Context onMessage = this.metricRegistry.timer(MetricRegistry.name(UmdsController.class,"authenticate")).time();
        try {
            LOG.info("Authentication required for :\n   [{}]", submission);
            if (invalidSender.equalsIgnoreCase(submission.getUser_identifier())) {
                LOG.warn("Wrong identifier:[{}], message should not be propagated",submission.getUser_identifier());
                return false;
            }
            certificateLogging.decodeAndlog(submission.getCertficiate());
            LOG.info("Authenticated");

            return true;
        }finally {
            onMessage.stop();
        }
    }

    //for testing purpose.
    @RequestMapping(value = "/message", method = RequestMethod.GET)
    public String onMessage() {
        return "Taxud UUMDS is up";
        /*Submission submission = new Submission();
        submission.setFromRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        submission.setToRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        submission.addFromParty("domibus-blue", "urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        submission.addToParty("domibus-red", "urn:oasis:names:tc:ebcore:partyid-type:unregistered");

        submission.addMessageProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:wrong#sender");
        submission.addMessageProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        submission.setAction("action");
        submission.setService("service");
        return submission;*/
    }


}
