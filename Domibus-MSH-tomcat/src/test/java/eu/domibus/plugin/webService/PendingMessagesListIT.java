package eu.domibus.plugin.webService;

import eu.domibus.AbstractIT;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.NotificationType;
import eu.domibus.messaging.NotifyMessageCreator;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.ListPendingMessagesResponse;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This JUNIT implements the Test cases List Pending Messages-01 and List Pending Messages-02.
 *
 * @author martifp
 */
public class PendingMessagesListIT extends AbstractIT {

    @Autowired
    JMSManager jmsManager;

    @Autowired
    BackendInterface backendWebService;

    @Test
    public void testListPendingMessagesOk() throws Exception {
        List<String> messageIds = new ArrayList<>();
        messageIds.add("2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu");
        messageIds.add("78a1d578-0cc7-41fb-9f35-86a5b2769a14@domibus.eu");
        messageIds.add("2bbc05d8-b603-4742-a118-137898a81de3@domibus.eu");

        for (String messageId : messageIds) {
            final JmsMessage message = new NotifyMessageCreator(messageId, NotificationType.MESSAGE_RECEIVED, new HashMap<>()).createMessage();
            jmsManager.sendMessageToQueue(message, WS_NOT_QUEUE);
        }

        String request = new String("<listPendingMessagesRequest></listPendingMessagesRequest>");
        ListPendingMessagesResponse response = backendWebService.listPendingMessages(request);


        // Verifies the response
        Assert.assertNotNull(response);
        Assert.assertFalse(response.getMessageID().isEmpty());
        Assert.assertTrue(response.getMessageID().containsAll(messageIds));

    }

    @Test
    public void testListPendingMessagesNOk() throws Exception {

        String request = new String("<listPendingMessagesRequest>1</listPendingMessagesRequest>");
        ListPendingMessagesResponse response = backendWebService.listPendingMessages(request);

        // Verifies the response
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getMessageID().isEmpty());
    }

}
