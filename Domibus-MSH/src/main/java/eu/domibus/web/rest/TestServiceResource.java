package eu.domibus.web.rest;

import eu.domibus.api.party.PartyService;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.handler.DatabaseMessageHandler;
import eu.domibus.web.rest.ro.TestServiceRequestRO;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping(value = "sender", method = RequestMethod.GET)
    public String getSenderParty() {
        return partyService.getGatewayPartyIdentifier();
    }

    @RequestMapping(value = "parties", method = RequestMethod.GET)
    public List<String> getTestParties() {
        return partyService.findPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
    }

    @RequestMapping(value = "submit", method = RequestMethod.POST)
    @ResponseBody
    public String submitTest(@RequestBody TestServiceRequestRO testServiceRequestRO) throws IOException, MessagingProcessingException {
        return databaseMessageHandler.submitTestMessage(testServiceRequestRO.getSender(), testServiceRequestRO.getReceiver());
    }

    @RequestMapping(value = "submitDynamicDiscovery", method = RequestMethod.POST)
    @ResponseBody
    public String submitTestDynamicDiscovery(@RequestBody TestServiceRequestRO testServiceRequestRO) throws IOException, MessagingProcessingException {
        return databaseMessageHandler.submitTestDynamicDiscoveryMessage(testServiceRequestRO.getSender(),
                    testServiceRequestRO.getReceiver(), testServiceRequestRO.getReceiverType(), testServiceRequestRO.getServiceType());
    }
}
