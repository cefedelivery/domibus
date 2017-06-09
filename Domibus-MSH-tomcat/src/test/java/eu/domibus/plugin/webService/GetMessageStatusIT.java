package eu.domibus.plugin.webService;

import eu.domibus.AbstractIT;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.GetStatusRequest;
import eu.domibus.plugin.webService.generated.MessageStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;


/**
 * Created by draguio on 16/02/2016.
 */
public class GetMessageStatusIT extends AbstractIT {

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
    public void testGetMessageStatusReceived() {
        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";
        GetStatusRequest messageStatusRequest = createMessageStatusRequest(messageId);
        MessageStatus response = backendWebService.getMessageStatus(messageStatusRequest);
        Assert.assertEquals(MessageStatus.RECEIVED, response);
    }

    @Test
    public void testGetMessageStatusInvalidId() {
        String invalidMessageId = "invalid";
        GetStatusRequest messageStatusRequest = createMessageStatusRequest(invalidMessageId);
        MessageStatus response = backendWebService.getMessageStatus(messageStatusRequest);
        Assert.assertEquals(MessageStatus.NOT_FOUND, response);
    }

    private GetStatusRequest createMessageStatusRequest(final String messageId) {
        GetStatusRequest messageStatusRequest = new GetStatusRequest();
        messageStatusRequest.setMessageID(messageId);
        return messageStatusRequest;
    }
}
