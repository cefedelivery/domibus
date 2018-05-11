
package eu.domibus.plugin.jms;


import eu.domibus.AbstractIT;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessagingService;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.webService.generated.MshRole;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.Date;

/**
 * This JUNIT implements the Test cases Download Message-03 and Download Message-04.
 * It uses the JMS backend connector.
 *
 * @author martifp
 */
public class DownloadMessageJMSIT extends AbstractIT {

    @Autowired
    private ConnectionFactory xaJmsConnectionFactory;

    @Autowired
    BackendConnector backendJms;

    @Autowired
    MessagingService messagingService;

    @Autowired
    UserMessageLogService userMessageLogService;

    @Before
    public void before() throws IOException, XmlProcessingException {
        final byte[] pmodeBytes = IOUtils.toByteArray(new ClassPathResource("dataset/pmode/PModeTemplate.xml").getInputStream());
        final Configuration pModeConfiguration = pModeProvider.getPModeConfiguration(pmodeBytes);
        configurationDAO.updateConfiguration(pModeConfiguration);
    }

    /**
     * Negative test: the message is not found in the JMS queue and a specific exception is returned.
     *
     * @throws RuntimeException
     */
    @Test(expected = RuntimeException.class)
    public void testDownloadMessageInvalidId() throws RuntimeException {

        // Prepare the request to the backend
        String messageId = "invalid@e-delivery.eu";

        backendJms.deliverMessage(messageId);

        Assert.fail("DownloadMessageFault was expected but was not raised");
    }

    /**
     * Tests that a message is found in the JMS queue and pushed to the business queue.
     *
     * @throws RuntimeException
     * @throws JMSException
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    public void testDownloadMessageOk() throws Exception {
        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";
        final UserMessage userMessage = getUserMessageTemplate();
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<hello>world</hello>";
        userMessage.getPayloadInfo().getPartInfo().iterator().next().setBinaryData(messagePayload.getBytes());
        userMessage.getPayloadInfo().getPartInfo().iterator().next().setMime("text/xml");
        userMessage.getPayloadInfo().getPartInfo().iterator().next().setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(messagePayload.getBytes(), "text/xml")));
        userMessage.getMessageInfo().setMessageId(messageId);
        eu.domibus.ebms3.common.model.Messaging messaging = new eu.domibus.ebms3.common.model.Messaging();
        messaging.setUserMessage(userMessage);
        messagingService.storeMessage(messaging, MSHRole.RECEIVING);

        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageStatus(eu.domibus.common.MessageStatus.RECEIVED);
        userMessageLog.setMessageId(messageId);
        userMessageLog.setMessageType(MessageType.USER_MESSAGE);
        userMessageLog.setMshRole(MSHRole.RECEIVING);
        userMessageLog.setReceived(new Date());
        userMessageLogService.save(messageId, eu.domibus.common.MessageStatus.RECEIVED.name(), NotificationStatus.REQUIRED.name(), MshRole.RECEIVING.name(), 1, "default", "backendWebservice", "");


        javax.jms.Connection connection = xaJmsConnectionFactory.createConnection("domibus", "changeit");
        connection.start();
        pushQueueMessage(messageId, connection, JMS_NOT_QUEUE_NAME);

        backendJms.deliverMessage(messageId);

        Message message = popQueueMessageWithTimeout(connection, JMS_BACKEND_OUT_QUEUE_NAME, 2000);
        Assert.assertNotNull(message);

        connection.close();
    }


}
