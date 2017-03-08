package eu.domibus.plugin;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.NotificationType;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.messaging.MessageNotFoundException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * // TODO reach 70% coverage.
 *
 * @author Arun Venugopal
 * @author Federico Martini
 * @since 3.3
 */
@RunWith(JMockit.class)
public class NotificationListenerServiceTest {

    private static final String TEST_FINAL_RECIPIENT = "TEST_FINAL_RECIPIENT1";
    private static final String TEST_FINAL_RECIPIENT2 = "ANOTHER_FINAL_RECIPIENT";

    @Injectable
    private Queue queue;

    @Injectable
    private BackendConnector.Mode mode;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    JMSManager jmsManager;

    @Injectable
    private JmsListenerContainerFactory internalJmsListenerContainerFactory;

    @Injectable
    private Properties domibusProperties;

    @Injectable
    BackendConnectorDelegate backendConnectorDelegate;

    @Tested
    NotificationListenerService objNotificationListenerService;


    @Test
    public void testAddToListFromQueueHappyFlow(final @Injectable QueueBrowser queueBrowser) throws JMSException {
        mode = BackendConnector.Mode.PULL;

        final List<JmsMessage> messages = generateTestMessages();

        new Expectations() {{

            domibusProperties.getProperty(NotificationListenerService.PROP_LIST_PENDING_MESSAGES_MAXCOUNT, "500");
            result = 5;

            jmsManager.browseMessages(withAny(new String()));
            result = messages;

        }};

        Collection<String> result = new ArrayList<String>();
        result.addAll(objNotificationListenerService.browseQueue(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT));
        Assert.assertEquals(5, result.size());
    }

    @Test
    public void testAddToListFromQueueMissingConfiguration(final @Injectable QueueBrowser queueBrowser) throws JMSException {
        mode = BackendConnector.Mode.PULL;

        final List<JmsMessage> messages = generateTestMessages();

        new Expectations() {{

            domibusProperties.getProperty(NotificationListenerService.PROP_LIST_PENDING_MESSAGES_MAXCOUNT, "500");
            result = 500;

            jmsManager.browseMessages(withAny(new String()));
            result = messages;

        }};

        /* Expected scenario when max pending messages configuration is not specified */
        Collection<String> result = new ArrayList<String>();
        result.addAll(objNotificationListenerService.browseQueue(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT));
        Assert.assertEquals(10, result.size());
    }

    @Test
    public void testRemoveFromPendingOk() throws Exception {

        mode = BackendConnector.Mode.PULL;

        final String messageId = "ID1";
        final String queueName = "eDeliveryModule!DomibusNotifyBackendWebServiceQueue";

        final List<JmsMessage> messages = generateTestMessages();

        new Expectations() {{

            objNotificationListenerService.getBackendNotificationQueue().getQueueName();
            result = queueName;

            jmsManager.consumeMessage(queueName, messageId);
            result = messages.get(0);

        }};

        objNotificationListenerService.removeFromPending(messageId);

        new Verifications() {{
            objNotificationListenerService.getBackendNotificationQueue().getQueueName();
            jmsManager.consumeMessage(queueName, messageId);
        }};
    }

    @Test
    public void removeFromPendingNOk() throws Exception {

        mode = BackendConnector.Mode.PULL;

        final String messageId = "ID1";
        final String queueName = "eDeliveryModule!DomibusNotifyBackendWebServiceQueue";

        new Expectations() {{

            objNotificationListenerService.getBackendNotificationQueue().getQueueName();
            result = queueName;

            jmsManager.consumeMessage(queueName, messageId);
            result = null;

        }};

        try {
            objNotificationListenerService.removeFromPending(messageId);
        } catch (MessageNotFoundException mnfEx) {
            Assert.assertEquals(mnfEx.getMessage(), "No message with id [" + messageId + "] pending for download");
        }

        new Verifications() {{
            objNotificationListenerService.getBackendNotificationQueue().getQueueName();
            jmsManager.consumeMessage(queueName, messageId);
        }};

    }

    private List<JmsMessage> generateTestMessages() throws JMSException {

        List<JmsMessage> testInputList = new ArrayList<>(12);

        testInputList.add(generateTestMessage(NotificationType.MESSAGE_SEND_SUCCESS, TEST_FINAL_RECIPIENT, "ID1"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT2, "ID2"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID3"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID4"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID5"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID6"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID7"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID8"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID9"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID10"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID11"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID12"));

        return testInputList;
    }

    private JmsMessage generateTestMessage(NotificationType notificationType, String recipient, String messageid) throws JMSException {
        JmsMessage message = new JmsMessage();
        message.setProperty(MessageConstants.NOTIFICATION_TYPE, notificationType.name());
        message.setProperty((MessageConstants.FINAL_RECIPIENT), recipient);
        message.setProperty((MessageConstants.MESSAGE_ID), messageid);
        return message;
    }


}
