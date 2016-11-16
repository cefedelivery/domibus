package eu.domibus.plugin.webService;

import eu.domibus.AbstractSendMessageIT;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.SendMessageFault;
import eu.domibus.plugin.webService.generated.SendRequest;
import eu.domibus.plugin.webService.generated.SendResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by draguio on 17/02/2016.
 */
public class SendMessageWithPayloadProfileIT extends AbstractSendMessageIT {

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
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSendMessageValid() throws SendMessageFault, InterruptedException, SQLException {

        //TODO Prepare the request to the backend
        String payloadHref = "payload";
        SendRequest sendRequest = createSendRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref);

        super.prepareSendMessage("validAS4Response2.xml");
        SendResponse response = backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
        verifySendMessageAck(response);
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled, no mime-type specified on payload
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSendMessageValidNoMimeType() throws SendMessageFault, InterruptedException, SQLException {

        //TODO Prepare the request to the backend
        String payloadHref = "payload";
        SendRequest sendRequest = createSendRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref, null);

        super.prepareSendMessage("validAS4Response.xml");
        SendResponse response = backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
        verifySendMessageAck(response);
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled and invalid payload Href
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test(expected = SendMessageFault.class)
    public void testSendMessageInvalidPayloadHref() throws SendMessageFault, InterruptedException {

        String payloadHref = "payload_invalid";
        SendRequest sendRequest = createSendRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref);
        super.prepareSendMessage("validAS4Response.xml");

        try {
            backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
        } catch (SendMessageFault re) {
            String message = "Message submission failed";
            Assert.assertEquals(message, re.getMessage());
            throw re;
        }
        Assert.fail("SendMessageFault was expected but was not raised");
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test(expected = SendMessageFault.class)
    public void testSendMessagePayloadHrefMismatch() throws SendMessageFault, InterruptedException {

        String payloadHref = "payload";
        SendRequest sendRequest = createSendRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref + "000");
        super.prepareSendMessage("validAS4Response.xml");

        try {
            backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);
        } catch (SendMessageFault re) {
            String message = "No payload found for PartInfo with href: payload000";
            String faultMsg = payloadHref + "000";
            Assert.assertEquals(message, re.getMessage());
            Assert.assertEquals(faultMsg, re.getFaultInfo().getMessage());
            throw re;
        }
        Assert.fail("SendMessageFault was expected but was not raised");
    }
}
