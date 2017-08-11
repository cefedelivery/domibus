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

package eu.domibus.ebms3.sender;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.ReceptionAwareness;
import eu.domibus.common.model.configuration.RetryStrategy;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class UpdateRetryLoggingServiceTest {

    private static final int RETRY_TIMEOUT_IN_MINUTES = 10;
    private static final long SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016 = 1451602800000L; //This is the reference time returned by System.correntTImeMillis() mock
    private static final long FIVE_MINUTES_BEFORE_FIRST_OF_JANUARY_2016 = SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016 - (60 * 5 * 1000);
    private static final long ONE_HOUR_BEFORE_FIRST_OF_JANUARY_2016 = SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016 - (60 * 60 * 1000);

    private static final String DELETE_PAYLOAD_ON_SEND_FAILURE = "domibus.sendMessage.failure.delete.payload";

    @Tested
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    private UserMessageLogDao messageLogDao;

    @Injectable
    private MessagingDao messagingDao;

    private LegConfiguration legConfiguration = new LegConfiguration();

    @Injectable
    private Properties domibusProperties;

    @Before
    public void setupExpectations() {
        new NonStrictExpectations(legConfiguration) {{
            legConfiguration.getReceptionAwareness();
            result = new ReceptionAwareness();
            legConfiguration.getReceptionAwareness().getStrategy().getAlgorithm();
            result = RetryStrategy.CONSTANT.getAlgorithm();
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = RETRY_TIMEOUT_IN_MINUTES;
        }};
    }


    /**
     * Max retries limit reached
     * Timeout limit not reached
     * Notification is enabled
     * Expected result: MessageLogDao#setAsNotified() is called
     *                  MessageLogDao#setMessageAsSendFailure is called
     *                  MessagingDao#clearPayloadData() is called
     *
     * @throws Exception
     */
    @Test
    public void testUpdateRetryLogging_maxRetriesReachedNotificationEnabled_ExpectedMessageStatus() throws Exception {
        new SystemMockFirstOfJanuary2016(); //current timestamp

        final String messageId = UUID.randomUUID().toString();
        final long receivedTime = FIVE_MINUTES_BEFORE_FIRST_OF_JANUARY_2016; //Received 5 min ago

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(3);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setReceived(new Date(receivedTime));
        userMessageLog.setNotificationStatus(NotificationStatus.REQUIRED);

        new Expectations() {{
            domibusProperties.getProperty(DELETE_PAYLOAD_ON_SEND_FAILURE, "false");
            result = true;

            messageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;
        }};


        updateRetryLoggingService.updateRetryLogging(messageId, legConfiguration);


        new Verifications() {{
            messageLogDao.setMessageAsSendFailure(messageId);
            messagingDao.clearPayloadData(messageId);
        }};

    }


    /**
     * Max retries limit reached
     * Timeout limit not reached
     * Notification is disabled
     * Expected result: MessageLogDao#setAsNotified() is not called
     *                  MessageLogDao#setMessageAsSendFailure is called
     *                  MessagingDao#clearPayloadData() is called
     *
     * @throws Exception
     */
    @Test
    public void testUpdateRetryLogging_maxRetriesReachedNotificationDisabled_ExpectedMessageStatus() throws Exception {
        new SystemMockFirstOfJanuary2016();

        final String messageId = UUID.randomUUID().toString();
        final long receivedTime = FIVE_MINUTES_BEFORE_FIRST_OF_JANUARY_2016; //Received 5 min ago

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(3);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setReceived(new Date(receivedTime));
        userMessageLog.setNotificationStatus(NotificationStatus.NOT_REQUIRED);

        new Expectations() {{
            domibusProperties.getProperty(DELETE_PAYLOAD_ON_SEND_FAILURE, "false");
            result = true;

            messageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;
        }};

        updateRetryLoggingService.updateRetryLogging(messageId, legConfiguration);

        new Verifications() {{
            messagingDao.clearPayloadData(messageId);
            messageLogDao.setMessageAsSendFailure(messageId);
            messageLogDao.setAsNotified(messageId); times = 0;
        }};

    }

    /**
     * Max retries limit reached
     * Notification is disabled
     * Clear payload is default (false)
     * Expected result: MessagingDao#clearPayloadData is not called
     *                  MessageLogDao#setMessageAsSendFailure is called
     *                  MessageLogDao#setAsNotified() is not called
     *
     * @throws Exception
     */
    @Test
    public void testUpdateRetryLogging_maxRetriesReachedNotificationDisabled_ExpectedMessageStatus_ClearPayloadDisabled() throws Exception {
        new SystemMockFirstOfJanuary2016();

        final String messageId = UUID.randomUUID().toString();
        final long receivedTime = FIVE_MINUTES_BEFORE_FIRST_OF_JANUARY_2016; //Received 5 min ago

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(3);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setReceived(new Date(receivedTime));
        userMessageLog.setNotificationStatus(NotificationStatus.NOT_REQUIRED);

        new Expectations() {{
            messageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;
        }};

        updateRetryLoggingService.updateRetryLogging(messageId, legConfiguration);

        new Verifications() {{
            messagingDao.clearPayloadData(messageId); times = 0;
            messageLogDao.setMessageAsSendFailure(messageId);
            messageLogDao.setAsNotified(messageId); times = 0;
        }};

    }

    /**
     * Max retries limit not reached
     * Timeout limit reached
     * Notification is enabled
     * Expected result: MessagingDao#clearPayloadData is called
     *                  MessageLogDao#setMessageAsSendFailure is called
     *                  MessageLogDao#setAsNotified() is called
     *
     * @throws Exception
     */
    @Test
    public void testUpdateRetryLogging_timeoutNotificationEnabled_ExpectedMessageStatus() throws Exception {
        new SystemMockFirstOfJanuary2016();

        final String messageId = UUID.randomUUID().toString();
        final long received = ONE_HOUR_BEFORE_FIRST_OF_JANUARY_2016; // received one hour ago

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(0);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setReceived(new Date(received));
        userMessageLog.setNotificationStatus(NotificationStatus.REQUIRED);

        new Expectations() {{
            domibusProperties.getProperty(DELETE_PAYLOAD_ON_SEND_FAILURE, "false");
            result = true;

            messageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;
        }};


        updateRetryLoggingService.updateRetryLogging(messageId, legConfiguration);


        new Verifications() {{
            messageLogDao.setMessageAsSendFailure(messageId);
            messagingDao.clearPayloadData(messageId);
        }};

    }


    /**
     * Max retries limit not reached
     * Timeout limit reached
     * Notification is disableds
     * Expected result: MessagingDao#clearPayloadData is called
     *                  MessageLogDao#setMessageAsSendFailure is called
     *                  MessageLogDao#setAsNotified() is called
     *
     * @throws Exception
     */
    @Test
    public void testUpdateRetryLogging_timeoutNotificationDisabled_ExpectedMessageStatus() throws Exception {
        new SystemMockFirstOfJanuary2016();

        final String messageId = UUID.randomUUID().toString();
        final long received = ONE_HOUR_BEFORE_FIRST_OF_JANUARY_2016; // received one hour ago

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(0);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setReceived(new Date(received));
        userMessageLog.setNotificationStatus(NotificationStatus.REQUIRED);

        new Expectations() {{
            domibusProperties.getProperty(DELETE_PAYLOAD_ON_SEND_FAILURE, "false");
            result = true;

            messageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;
        }};


        updateRetryLoggingService.updateRetryLogging(messageId, legConfiguration);


        new Verifications() {{
            messageLogDao.setMessageAsSendFailure(messageId);
            messagingDao.clearPayloadData(messageId);
        }};

    }

    /**
     * Max retries limit not reached
     * Timeout limit not reached
     * Expected result:
     * UserMessageLog#getMessageStatus() == WAITING_FOR_RETRY
     * UserMessageLog#getSendAttempts() == 1
     *
     * @throws Exception
     */
    @Test
    public void testUpdateRetryLogging_success_ExpectedMessageStatus() throws Exception {
        new SystemMockFirstOfJanuary2016();

        final String messageId = UUID.randomUUID().toString();
        final long received = FIVE_MINUTES_BEFORE_FIRST_OF_JANUARY_2016;


        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(0);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setReceived(new Date(received));

        new Expectations() {{
            messageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;
        }};


        updateRetryLoggingService.updateRetryLogging(messageId, legConfiguration);


        assertEquals(MessageStatus.WAITING_FOR_RETRY, userMessageLog.getMessageStatus());
        assertEquals(1, userMessageLog.getSendAttempts());

    }

    private static class SystemMockFirstOfJanuary2016 extends MockUp<System> {
        @Mock
        public static long currentTimeMillis() {
            return SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016;
        }
    }
}