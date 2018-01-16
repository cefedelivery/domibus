package eu.domibus.plugin.webService;

import eu.domibus.AbstractSendMessageIT;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.ebms3.sender.NonRepudiationChecker;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.SubmitMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.plugin.webService.generated.SubmitResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Created by draguio on 17/02/2016.
 */
public class SubmitMessageSignOnlyIT extends AbstractSendMessageIT {

    private static boolean initialized;

    @Autowired
    BackendInterface backendWebService;

    @Autowired
    NonRepudiationChecker nonRepudiationChecker;

    @InjectMocks
    @Autowired
    private ReliabilityChecker reliabilityChecker;

    @Before
    public void before() throws IOException {
        // Initialize the mock objects
        MockitoAnnotations.initMocks(this);

        if (!initialized) {
            // The dataset is executed only once for each class
            insertDataset("sendMessageSignOnlyDataset.sql");
            initialized = true;
        }
    }


    /**
     * Test for the backend sendMessage service with payload profile enabled
     *
     * @throws SubmitMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSubmitMessageValid() throws SubmitMessageFault, InterruptedException {

        String payloadHref = "payload";
        //SendRequest sendRequest = createSendRequest(payloadHref);
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref);

        super.prepareSendMessage("validAS4Response.xml");
        //SendResponse response = backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
        SubmitResponse response = backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);

        TimeUnit.SECONDS.sleep(4);

        Assert.assertNotNull(response);

        verify(postRequestedFor(urlMatching("/domibus/services/msh"))
                .withRequestBody(containing("DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\""))
                .withHeader("Content-Type", notMatching("application/soap+xml")));
    }
}
