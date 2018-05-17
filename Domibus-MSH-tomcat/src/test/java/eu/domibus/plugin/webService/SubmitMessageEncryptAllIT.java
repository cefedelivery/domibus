package eu.domibus.plugin.webService;


import eu.domibus.AbstractBackendWSIT;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.webService.generated.SubmitMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.plugin.webService.generated.SubmitResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by draguio on 17/02/2016.
 */
@DirtiesContext
@Rollback
public class SubmitMessageEncryptAllIT extends AbstractBackendWSIT {

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled
     *
     * @throws SubmitMessageFault
     * @throws InterruptedException
     */

    @Test
    public void testSubmitMessageValid() throws SubmitMessageFault {
        String payloadHref = "cid:message";
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref);

        super.prepareSendMessage("validAS4Response.xml");
        SubmitResponse response = backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);

        final List<String> messageID = response.getMessageID();
        assertNotNull(response);
        assertNotNull(messageID);
        assertTrue(messageID.size() == 1);
        final String messageId = messageID.iterator().next();

        //message will fail as the response message does not contain the right security details(signature, etc)
        waitUntilMessageIsInWaitingForRetry(messageId);

        verify(postRequestedFor(urlMatching("/domibus/services/msh"))
                .withRequestBody(containing("EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\""))
                .withHeader("Content-Type", notMatching("application/soap+xml")));
    }
}
