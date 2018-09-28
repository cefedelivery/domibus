package eu.domibus.web.rest;

import eu.domibus.api.party.PartyService;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.web.rest.ro.TestServiceRequestRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestServiceResource.class);

    @Autowired
    protected TestService testService;

    @Autowired
    private PartyService partyService;

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorRO> handleException(Exception ex) {
        LOG.error(ex.getMessage(), ex);

        ErrorRO error = new ErrorRO(ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONNECTION, "close");

        return new ResponseEntity(error, headers, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "sender", method = RequestMethod.GET)
    public String getSenderParty() {
        return partyService.getGatewayPartyIdentifier();
    }

    @RequestMapping(value = "parties", method = RequestMethod.GET)
    public List<String> getTestParties() {
        return partyService.findPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String submitTest(@RequestBody TestServiceRequestRO testServiceRequestRO) throws IOException, MessagingProcessingException {
        return testService.submitTest(testServiceRequestRO.getSender(),  testServiceRequestRO.getReceiver());
    }

    @RequestMapping(value = "dynamicdiscovery", method = RequestMethod.POST)
    @ResponseBody
    public String submitTestDynamicDiscovery(@RequestBody TestServiceRequestRO testServiceRequestRO) throws IOException, MessagingProcessingException {
        return testService.submitTestDynamicDiscovery(testServiceRequestRO.getSender(),  testServiceRequestRO.getReceiver(), testServiceRequestRO.getReceiverType());
    }
}
