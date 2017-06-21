package eu.domibus.plugin.webService;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.domibus.AbstractSendMessageIT;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.ebms3.sender.NonRepudiationChecker;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.SendMessageFault;
import eu.domibus.plugin.webService.generated.SendRequest;
import eu.domibus.plugin.webService.generated.SendResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Created by draguio on 17/02/2016.
 */
public class SendMessageSignOnlyIT extends AbstractSendMessageIT {

    private static boolean initialized;

    @Autowired
    BackendInterface backendWebService;

    @Mock
    NonRepudiationChecker nonRepudiationChecker;

    @InjectMocks
    @Autowired
    private ReliabilityChecker reliabilityChecker;

    @Before
    public void before() throws IOException {
        // Initialize the mock objects
        MockitoAnnotations.initMocks(this);
        Mockito.when(nonRepudiationChecker.compareUnorderedReferenceNodeLists(Mockito.any(NodeList.class), Mockito.any(NodeList.class))).thenReturn(true);

        if (!initialized) {
            // The dataset is executed only once for each class
            insertDataset("sendMessageSignOnlyDataset.sql");
            initialized = true;
        }
    }


    /**
     * Test for the backend sendMessage service with payload profile enabled
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSendMessageValid() throws SendMessageFault, InterruptedException {

        String payloadHref = "payload";
        SendRequest sendRequest = createSendRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref);

        super.prepareSendMessage("validAS4Response.xml");
        SendResponse response = backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);

        TimeUnit.SECONDS.sleep(4);

        Assert.assertNotNull(response);

        verify(postRequestedFor(urlMatching("/domibus/services/msh"))
                .withRequestBody(containing("DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\""))
                .withHeader("Content-Type", notMatching("application/soap+xml")));
    }
}
