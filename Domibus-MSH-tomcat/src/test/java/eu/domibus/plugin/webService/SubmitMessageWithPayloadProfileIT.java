package eu.domibus.plugin.webService;

import eu.domibus.AbstractSendMessageIT;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.SubmitMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.plugin.webService.generated.SubmitResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by draguio on 17/02/2016.
 */
@Ignore
public class SubmitMessageWithPayloadProfileIT extends AbstractSendMessageIT {

    private static boolean initialized;
    @Autowired
    BackendInterface backendWebService;

    @Before
    public void before() throws IOException {
        if (!initialized) {
            // The dataset is executed only once for each class
            insertDataset("sendMessageWithPayloadProfileDataset.sql");
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
    public void testSubmitMessageValid() throws SubmitMessageFault, InterruptedException, SQLException {

        //TODO Prepare the request to the backend
        String payloadHref = "payload";
        //SendRequest sendRequest = createSendRequest(payloadHref);
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref);

        super.prepareSendMessage("validAS4Response2.xml");
        //SendResponse response = backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
        SubmitResponse response = backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);
        verifySendMessageAck(response);
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled, no mime-type specified on payload
     *
     * @throws SubmitMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSubmitMessageValidNoMimeType() throws SubmitMessageFault, InterruptedException, SQLException {

        //TODO Prepare the request to the backend
        String payloadHref = "payload";
        //SendRequest sendRequest = createSendRequest(payloadHref);
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref, null);

        super.prepareSendMessage("validAS4Response.xml");
        //SendResponse response = backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
        SubmitResponse response = backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);
        verifySendMessageAck(response);
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled and invalid payload Href
     *
     * @throws SubmitMessageFault
     * @throws InterruptedException
     */
    @Test(expected = SubmitMessageFault.class)
    public void testSubmitMessageInvalidPayloadHref() throws SubmitMessageFault, InterruptedException {

        String payloadHref = "payload_invalid";
        //SendRequest sendRequest = createSendRequest(payloadHref);
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref);
        super.prepareSendMessage("validAS4Response.xml");

        try {
            //backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
            backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);
        } catch (SubmitMessageFault re) {
            String message = "Message submission failed";
            Assert.assertEquals(message, re.getMessage());
            throw re;
        }
        Assert.fail("SubmitMessageFault was expected but was not raised");
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled
     *
     * @throws SubmitMessageFault
     * @throws InterruptedException
     */
    @Test(expected = SubmitMessageFault.class)
    public void testSubmitMessagePayloadHrefMismatch() throws SubmitMessageFault, InterruptedException {

        String payloadHref = "payload";
        //SendRequest sendRequest = createSendRequest(payloadHref);
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref + "000");
        super.prepareSendMessage("validAS4Response.xml");

        try {
            //backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
            backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);
        } catch (SubmitMessageFault re) {
            String message = "No payload found for PartInfo with href: payload000";
            String faultMsg = payloadHref + "000";
            Assert.assertEquals(message, re.getMessage());
            Assert.assertEquals(faultMsg, re.getFaultInfo().getMessage());
            throw re;
        }
        Assert.fail("SubmitMessageFault was expected but was not raised");
    }
}
