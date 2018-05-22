package eu.domibus.web.rest;

import com.thoughtworks.xstream.XStream;
import eu.domibus.api.party.PartyService;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.DatabaseMessageHandler;
import eu.domibus.web.rest.ro.TestServiceRequestRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@RestController
@RequestMapping(value = "/rest/testservice")
public class TestServiceResource {

    @Autowired
    private PartyService partyService;

    @Autowired
    private DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    private PModeProvider pModeProvider;

    @RequestMapping(value = "sender", method = RequestMethod.GET)
    public String getSenderParty() {
        return partyService.getGatewayPartyIdentifier();
    }

    @RequestMapping(value = "parties", method = RequestMethod.GET)
    public List<String> getTestParties() {
        return partyService.findPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
    }

    private Submission createSubmission(String sender) throws IOException {
        Resource testservicefile = new ClassPathResource("messages/testservice/testservicemessage.xml");
        XStream xstream = new XStream();
        Submission submission = (Submission) xstream.fromXML(testservicefile.getInputStream());

        // Set Sender
        submission.getFromParties().clear();
        submission.getFromParties().add(new Submission.Party(sender, pModeProvider.getPartyIdType(sender)));

        // Set ServiceType
        submission.setServiceType(pModeProvider.getServiceType(Ebms3Constants.TEST_SERVICE));

        // Set From Role
        submission.setFromRole(pModeProvider.getRole("INITIATOR", Ebms3Constants.TEST_SERVICE));

        // Set To Role
        submission.setToRole(pModeProvider.getRole("RESPONDER", Ebms3Constants.TEST_SERVICE));

        // Set Agreement Ref
        submission.setAgreementRef(pModeProvider.getAgreementRef(Ebms3Constants.TEST_SERVICE));

        // Set Conversation Id
        // As the eb:ConversationId element is required it must always have a value.
        // If no value is included in the 301 submission of the business document to the Access Point,
        // the Access Point MUST set the value of 302 eb:ConversationId to “1” as specified in section 4.3 of [ebMS3CORE].
        submission.setConversationId("1");

        return submission;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String submitTest(@RequestBody TestServiceRequestRO testServiceRequestRO) throws IOException, MessagingProcessingException {

        Submission messageData = createSubmission(testServiceRequestRO.getSender());

        // Set Receiver
        messageData.getToParties().clear();
        String receiver = testServiceRequestRO.getReceiver();
        messageData.getToParties().add(new Submission.Party(receiver, pModeProvider.getPartyIdType(receiver)));

        return databaseMessageHandler.submit(messageData, "TestService");
    }

    @RequestMapping(value = "dynamicdiscovery", method = RequestMethod.POST)
    @ResponseBody
    public String submitTestDynamicDiscovery(@RequestBody TestServiceRequestRO testServiceRequestRO) throws IOException, MessagingProcessingException {
        Submission messageData = createSubmission(testServiceRequestRO.getSender());

        // Clears Receivers
        messageData.getToParties().clear();

        // Set Final Recipient Value and Type
        for(Submission.TypedProperty property : messageData.getMessageProperties()) {
            if(property.getKey().equals("finalRecipient")) {
                property.setValue(testServiceRequestRO.getReceiver());
                property.setType(testServiceRequestRO.getReceiverType());
            }
        }

        return databaseMessageHandler.submit(messageData, "TestService");
    }
}
