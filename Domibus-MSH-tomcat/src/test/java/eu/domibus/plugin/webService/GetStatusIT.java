package eu.domibus.plugin.webService;

import eu.domibus.AbstractIT;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessagingService;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.MessageStatus;
import eu.domibus.plugin.webService.generated.StatusFault;
import eu.domibus.plugin.webService.generated.StatusRequest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;

public class GetStatusIT extends AbstractIT {

    @Autowired
    BackendInterface backendWebService;

    @Autowired
    MessagingService messagingService;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    public void testGetStatusReceived() throws StatusFault, IOException {
        final UserMessage userMessage = getUserMessageTemplate();
        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";
        userMessage.getMessageInfo().setMessageId(messageId);
        Messaging messaging = new Messaging();
        messaging.setUserMessage(userMessage);
        messagingService.storeMessage(messaging, MSHRole.RECEIVING);

        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageStatus(eu.domibus.common.MessageStatus.RECEIVED);
        userMessageLog.setMessageId(messageId);
        userMessageLog.setMessageType(MessageType.USER_MESSAGE);
        userMessageLog.setMshRole(MSHRole.RECEIVING);
        userMessageLog.setReceived(new Date());
        userMessageLogDao.create(userMessageLog);

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
