package eu.domibus.plugin;

import eu.domibus.common.NotificationType;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.command.ActiveMQMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsOperations;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import java.util.*;

/**
 * @author venugar
 * @since 3.3
 */

@RunWith(JMockit.class)
public class NotificationListenerServiceTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NotificationListenerServiceTest.class);

    private static final String TEST_FINAL_RECIPIENT = "TEST_FINAL_RECIPIENT1";
    private static final String TEST_FINAL_RECIPIENT2 = "ANOTHER_FINAL_RECIPIENT";

    @Injectable
    private Queue queue;

    @Injectable
    private BackendConnector.Mode mode;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private JmsOperations jmsTemplateNotify;

    @Injectable
    private JmsListenerContainerFactory internalJmsListenerContainerFactory;

    @Injectable
    private Properties domibusProperties;

    @Tested
    NotificationListenerService objNotificationListenerService;


    @Test
    public void testAddToListFromQueueHappyFlow(final @Injectable QueueBrowser queueBrowser) throws JMSException {
        mode = BackendConnector.Mode.PULL;
        final Enumeration<Message> enumeration = generateTestInputEnumeration();

        new Expectations() {{
            queueBrowser.getEnumeration();
            result = enumeration;

        }};

        Collection<String> result1 = new ArrayList<String>();
        result1.addAll(objNotificationListenerService.listFromQueue(NotificationType.MESSAGE_RECEIVED, queueBrowser, TEST_FINAL_RECIPIENT, 5));
        Assert.assertEquals(5, result1.size());
    }

    @Test
    public void testAddToListFromQueueMissingConfiguration(final @Injectable QueueBrowser queueBrowser) throws JMSException {
        mode = BackendConnector.Mode.PULL;
        final Enumeration en = generateTestInputEnumeration();

        new Expectations() {{
            queueBrowser.getEnumeration();
            result = en;

        }};

        /*Expected scenario when max pending messages configuration is not specified*/
        Collection<String> result3 = new ArrayList<String>();
        result3.addAll(objNotificationListenerService.listFromQueue(NotificationType.MESSAGE_RECEIVED, queueBrowser, TEST_FINAL_RECIPIENT, 0));
        Assert.assertEquals(10, result3.size());
    }

    protected Enumeration<Message> generateTestInputEnumeration() throws JMSException, NullPointerException {

        List<Message> testInputList = new ArrayList<>(12);

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

        return Collections.enumeration(testInputList);
    }

    public Message generateTestMessage(NotificationType notificationType, String recipient, String messageid) throws JMSException {
        Message message = new ActiveMQMessage();
        message.setStringProperty(MessageConstants.NOTIFICATION_TYPE, notificationType.name());
        message.setStringProperty((MessageConstants.FINAL_RECIPIENT), recipient);
        message.setStringProperty((MessageConstants.MESSAGE_ID), messageid);

        return message;
    }


}
