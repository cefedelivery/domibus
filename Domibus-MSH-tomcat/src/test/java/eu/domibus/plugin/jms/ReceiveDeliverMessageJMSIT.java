
package eu.domibus.plugin.jms;

import eu.domibus.AbstractIT;
import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessagingService;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.webService.generated.MshRole;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;

import javax.activation.DataHandler;
import javax.jms.ConnectionFactory;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;

import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;
import static eu.domibus.plugin.jms.JMSMessageConstants.PAYLOAD_DESCRIPTION_FORMAT;


/**
 * This class implements the test cases Receive Deliver Message-01
 *
 * @author martifp
 */
public class ReceiveDeliverMessageJMSIT extends AbstractIT {


    @Autowired
    private BackendJMSImpl backendJMSImpl;

    @Autowired
    private ConnectionFactory xaJmsConnectionFactory;


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
     * It tests the message reception by Domibus through the JMS channel.
     * It also checks that the messages are actually pushed on the right queues (dispatch and reply).
     * The message ID is cleaned to simulate the submission of the a new message.
     *
     * @throws Exception
     */
    @Test
    @DirtiesContext
    public void testReceiveMessage() throws Exception {
        final MapMessage mapMessage = prepareMessageForSubmit();

        System.out.println("MapMessage: " + mapMessage);
        String messageId = UUID.randomUUID().toString();
        mapMessage.setStringProperty(MESSAGE_ID, messageId); // Cleaning the message ID since it is supposed to submit a new message.
        mapMessage.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_SUBMIT);
        mapMessage.setStringProperty(MessageFormat.format(PAYLOAD_DESCRIPTION_FORMAT, 1), "message");
        // The downloaded MapMessage is used as input parameter for the real Test case here!
        backendJMSImpl.receiveMessage(mapMessage);
        // Verifies that the message is really in the queue
        javax.jms.Connection connection = xaJmsConnectionFactory.createConnection("domibus", "changeit");
        connection.start();
        Message message = popQueueMessageWithTimeout(connection, JMS_BACKEND_REPLY_QUEUE_NAME, 2000);
        connection.close();
        Assert.assertEquals(message.getStringProperty(JMSMessageConstants.MESSAGE_ID), messageId);
        Assert.assertNull(message.getStringProperty("ErrorMessage"));

    }

    protected MapMessage prepareMessageForSubmit() throws Exception {
        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";
        final UserMessage userMessage = getUserMessageTemplate();
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<hello>world</hello>";
        final PartInfo partInfo = userMessage.getPayloadInfo().getPartInfo().iterator().next();
        partInfo.setBinaryData(messagePayload.getBytes());
        partInfo.setHref("message");
        final Property mimeTypeProperty = new Property();
        mimeTypeProperty.setName(Property.MIME_TYPE);
        mimeTypeProperty.setValue("text/xml");
        partInfo.getPartProperties().getProperties().add(mimeTypeProperty);

        partInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(messagePayload.getBytes(), "text/xml")));
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
        // Puts the message in the notification queue so it can be downloaded
        pushQueueMessage(messageId, connection, JMS_NOT_QUEUE_NAME);
        connection.close();

        final MapMessage mapMessage = new ActiveMQMapMessage();

        backendJMSImpl.downloadMessage(messageId, mapMessage);
        return mapMessage;
    }

    /**
     * Similar test to the previous one but this does not change the Message ID so that an exception is raised and handled with an JMS error message.
     * It tests that the message is actually into the REPLY queue.
     *
     * @throws Exception
     */
    @Test
    @DirtiesContext
    public void testDuplicateMessage() throws Exception {
        final MapMessage mapMessage = prepareMessageForSubmit();
        final String messageId = mapMessage.getStringProperty(MESSAGE_ID);

        System.out.println("MapMessage: " + mapMessage);
        mapMessage.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_SUBMIT);
        mapMessage.setStringProperty(MessageFormat.format(PAYLOAD_DESCRIPTION_FORMAT, 1), "message");
        // The downloaded MapMessage is used as input parameter for the real Test case here!
        backendJMSImpl.receiveMessage(mapMessage);
        // Verifies that the message is really in the queue
        javax.jms.Connection connection = xaJmsConnectionFactory.createConnection("domibus", "changeit");
        connection.start();
        Message message = popQueueMessageWithTimeout(connection, JMS_BACKEND_REPLY_QUEUE_NAME, 2000);
        connection.close();
        Assert.assertEquals(message.getStringProperty(JMSMessageConstants.MESSAGE_ID), messageId);
        Assert.assertNotNull(message.getStringProperty("ErrorMessage"));
    }

}
