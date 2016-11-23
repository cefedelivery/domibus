package eu.domibus.plugin;

import eu.domibus.common.NotificationType;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsOperations;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by venugar on 16/11/2016.
 */

@RunWith(JMockit.class)
public class NotificationListenerServiceTest {

    private static final Log LOG = LogFactory.getLog(NotificationListenerServiceTest.class);

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

        final Vector<Message> testInputVector = new Vector<>(10);
        loadTestInputMessages(testInputVector);

        new Expectations() {{
            queueBrowser.getEnumeration();
            result = testInputVector.elements();

        }};

        Collection<String> result1 = new ArrayList<String>();
        objNotificationListenerService.listFromQueue(NotificationType.MESSAGE_RECEIVED, queueBrowser, "TEST_FINAL_RECIPIENT", result1, 5);
        Assert.assertEquals(5, result1.size());

    }

    @Test
    public void testAddToListFromQueueMissingConfiguration(final @Injectable QueueBrowser queueBrowser) throws JMSException {
        mode = BackendConnector.Mode.PULL;

        final Vector<Message> testInputVector = new Vector<>(10);
        loadTestInputMessages(testInputVector);

        new Expectations() {{
            queueBrowser.getEnumeration();
            result = testInputVector.elements();

        }};

        /*Expected scenario when max pending messages configuration is not specified*/
        Collection<String> result3 = new ArrayList<String>();
        objNotificationListenerService.listFromQueue(NotificationType.MESSAGE_RECEIVED, queueBrowser, "TEST_FINAL_RECIPIENT", result3, 0);
        Assert.assertEquals(10, result3.size());
    }


    protected void loadTestInputMessages(Vector<Message> testInputVector) throws JMSException, NullPointerException {

        Message message11 = new ActiveMQMessage();
        message11.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_SEND_SUCCESS.name());
        message11.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message11.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a346710@domibus.eu");
        testInputVector.add(message11);

        Message message12 = new ActiveMQMessage();
        message12.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message12.setStringProperty((MessageConstants.FINAL_RECIPIENT), "ANOTHER_FINAL_RECIPIENT");
        message12.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a346710@domibus.eu");
        testInputVector.add(message12);

        Message message1 = new ActiveMQMessage();
        message1.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message1.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message1.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a34671@domibus.eu");
        testInputVector.add(message1);

        Message message2 = new ActiveMQMessage();
        message2.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message2.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message2.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a34672@domibus.eu");
        testInputVector.add(message2);

        Message message3 = new ActiveMQMessage();
        message3.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message3.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message3.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a34673@domibus.eu");
        testInputVector.add(message3);

        Message message4 = new ActiveMQMessage();
        message4.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message4.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message4.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a34674@domibus.eu");
        testInputVector.add(message4);

        Message message5 = new ActiveMQMessage();
        message5.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message5.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message5.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a34675@domibus.eu");
        testInputVector.add(message5);

        Message message6 = new ActiveMQMessage();
        message6.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message6.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message6.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a34676@domibus.eu");
        testInputVector.add(message6);

        Message message7 = new ActiveMQMessage();
        message7.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message7.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message7.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a34677@domibus.eu");
        testInputVector.add(message7);

        Message message8 = new ActiveMQMessage();
        message8.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message8.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message8.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a34678@domibus.eu");
        testInputVector.add(message8);

        Message message9 = new ActiveMQMessage();
        message9.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message9.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message9.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu");
        testInputVector.add(message9);

        Message message10 = new ActiveMQMessage();
        message10.setStringProperty(MessageConstants.NOTIFICATION_TYPE, NotificationType.MESSAGE_RECEIVED.name());
        message10.setStringProperty((MessageConstants.FINAL_RECIPIENT), "TEST_FINAL_RECIPIENT");
        message10.setStringProperty((MessageConstants.MESSAGE_ID), "2809cef6-240f-4792-bec1-7cb300a346710@domibus.eu");
        testInputVector.add(message10);
    }


}
