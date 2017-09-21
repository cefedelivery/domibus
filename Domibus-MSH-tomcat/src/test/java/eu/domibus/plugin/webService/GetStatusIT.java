package eu.domibus.plugin.webService;

import eu.domibus.AbstractIT;
import eu.domibus.plugin.webService.generated.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class GetStatusIT extends AbstractIT {

    @Autowired
    BackendInterface backendWebService;

    private static boolean initialized;

    @Before
    public void before() throws IOException {
        if (!initialized) {
            insertDataset("getMessageStatus.sql");
            initialized = true;
        }
    }

    @Test
    public void testGetStatusReceived() throws StatusFault {
        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";
        StatusRequest messageStatusRequest = createMessageStatusRequest(messageId);
        MessageStatus response = backendWebService.getStatus(messageStatusRequest);
        Assert.assertEquals(MessageStatus.RECEIVED, response);
    }

    @Test
    public void testGetStatusInvalidId() throws StatusFault {
        String invalidMessageId = "invalid";
        StatusRequest messageStatusRequest = createMessageStatusRequest(invalidMessageId);
        MessageStatus response = backendWebService.getStatus(messageStatusRequest);
        Assert.assertEquals(MessageStatus.NOT_FOUND, response);
    }

    @Test(expected = StatusFault.class)
    public void testGetStatusEmptyMessageId() throws StatusFault {
        String emptyMessageId = "";
        StatusRequest messageStatusRequest = createMessageStatusRequest(emptyMessageId);
        try {
            MessageStatus response = backendWebService.getStatus(messageStatusRequest);
        } catch (StatusFault statusFault) {
            String message = "Message ID is empty";
            Assert.assertEquals(message, statusFault.getMessage());
            throw statusFault;
        }
    }

    private StatusRequest createMessageStatusRequest(final String messageId) {
        StatusRequest statusRequest = new StatusRequest();
        statusRequest.setMessageID(messageId);
        return statusRequest;
    }
}
