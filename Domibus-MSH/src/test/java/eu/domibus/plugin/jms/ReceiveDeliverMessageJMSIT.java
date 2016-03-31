/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.plugin.jms;

import eu.domibus.AbstractIT;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.plugin.BackendConnector;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * This class implements the test cases Receive Deliver Message-01
 *
 * @author martifp
 */
public class ReceiveDeliverMessageJMSIT extends AbstractIT {

    private static boolean initialized;
    @Autowired
    @Qualifier("backendJms")
    BackendConnector backendJms;

    private BackendJMSImpl backendJMSImpl;

    @Autowired
    private ConnectionFactory xaJmsConnectionFactory;

    @Before
    public void before() throws Exception {

        backendJMSImpl = getTargetObject(backendJms);

        if (!initialized) {
            // The dataset is executed only once for each class
            insertDataset("receiveMessageJMS.sql");
            initialized = true;
        }
    }

    private void verifyMessageStatus(String messageId) throws SQLException {
        Connection con = dataSource.getConnection();
        String sql = "SELECT MESSAGE_ID, MESSAGE_STATUS FROM TB_MESSAGE_LOG WHERE MESSAGE_ID = ?";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, messageId);
        ResultSet resultSet = pstmt.executeQuery();
        resultSet.next();
        Assert.assertEquals(MessageStatus.SEND_ENQUEUED.name(), resultSet.getString("MESSAGE_STATUS"));
        pstmt.close();
    }

    /**
     * It tests the message reception by the backend and the delivery to the plugin (JMS queue).
     * It also checks that the messages are stored in the queues trying to receive them.
     *
     * @throws JMSException
     * @throws EbMS3Exception
     * @throws SQLException
     * @throws InterruptedException
     */
    // @Ignore
    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    public void testReceiveMessage() throws Exception {

        javax.jms.Connection connection = xaJmsConnectionFactory.createConnection("domibus", "changeit");
        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";
        pushQueueMessage(messageId, connection, "domibus.backend.jms.inQueue");
        // Puts the message in the notification queue so it can be downloaded
        connection = xaJmsConnectionFactory.createConnection("domibus", "changeit");
        pushQueueMessage(messageId, connection, JMS_NOT_QUEUE_NAME);
        final MapMessage mapMessage = new ActiveMQMapMessage();
        backendJMSImpl.downloadMessage(messageId, mapMessage);
        mapMessage.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_SUBMIT);
        // The downloaded MapMessage is used as input parameter for the real Test case here!
        backendJMSImpl.receiveMessage(mapMessage);
        // Verification on the message status
        //verifyMessageStatus(mapMessage.getStringProperty(MessageConstants.MESSAGE_ID)); // mapMessage.getStringProperty(MessageConstants.MESSAGE_ID) is NULL!
        // Verifies that the message is really in the queue
        connection = xaJmsConnectionFactory.createConnection("domibus", "changeit");
        Message message = popQueueMessage(connection, "domibus.backend.jms.outQueue");
        Assert.assertNotNull(message);
        System.out.println("Message: " + message);
        /*connection =  xaJmsConnectionFactory.createConnection("domibus", "changeit");
         message =  popQueueMessage(connection, "domibus.backend.jms.replyQueue");
        Assert.assertNotNull(message);
        System.out.println("Message: " + message);*/
    }


    private <T> T getTargetObject(Object proxy) throws Exception {
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return (T) ((Advised) proxy).getTargetSource().getTarget();
        } else {
            return (T) proxy;
        }
    }

}
