package eu.domibus.plugin.ws;

import eu.domibus.AbstractIT;
import eu.domibus.common.MessageStatus;
import eu.domibus.plugin.webService.MessageStatusRequest;
import eu.domibus.plugin.webService.generated.BackendInterface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by draguio on 16/02/2016.
 */
public class GetMessageStatusIT extends AbstractIT {

    @Autowired
    BackendInterface backendWebService;

    private static boolean initialized;

    @Before
    public void before() {
        if (!initialized) {
            insertDataset("getMessageStatus.sql");
            initialized = true;
        }
    }

    @Test
    public void testGetMessageStatusReceived() {
        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";
        MessageStatusRequest messageStatusRequest = createMessageStatusRequest(messageId);
        MessageStatus response = backendWebService.getMessageStatus(messageStatusRequest);

        Assert.assertEquals(MessageStatus.RECEIVED, response);
    }

    @Test
    public void testGetMessageStatusInvalidId() {
        String invalidMessageId = "invalid";
        MessageStatusRequest messageStatusRequest = createMessageStatusRequest(invalidMessageId);
        MessageStatus response = backendWebService.getMessageStatus(messageStatusRequest);

        Assert.assertEquals(MessageStatus.NOT_FOUND, response);
    }

    private MessageStatusRequest createMessageStatusRequest(final String messageId) {

        MessageStatusRequest messageStatusRequest = new MessageStatusRequest();
        messageStatusRequest.setMessageID(messageId);

        return messageStatusRequest;
    }
}
