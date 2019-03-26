package eu.domibus.common.services.impl;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@RunWith(JMockit.class)
public class MessageRetentionServiceTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private Queue retentionMessageQueue;

    @Tested
    MessageRetentionService messageRetentionService;

    @Test
    public void testDeleteExpiredMessages() {
        final String mpc1 = "mpc1";
        final String mpc2 = "mpc2";
        final List<String> mpcs = Arrays.asList(new String[]{mpc1, mpc2});

        new Expectations(messageRetentionService) {{
            pModeProvider.getMpcURIList();
            result = mpcs;

            messageRetentionService.getRetentionValue(MessageRetentionService.DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
            result = 10;

            messageRetentionService.getRetentionValue(MessageRetentionService.DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
            result = 20;
        }};

        messageRetentionService.deleteExpiredMessages();

        new Verifications() {{
            messageRetentionService.deleteExpiredMessages(mpc1, 10, 20);
        }};
    }

    @Test
    public void testDeleteExpiredMessagesForMpc() {
        final String mpc1 = "mpc1";
        final Integer expiredDownloadedMessagesLimit = 10;
        final Integer expiredNotDownloadedMessagesLimit = 20;

        new Expectations(messageRetentionService) {{
            //partial mocking of the following methods
            messageRetentionService.deleteExpiredDownloadedMessages(mpc1, expiredDownloadedMessagesLimit);
            messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, expiredNotDownloadedMessagesLimit);
        }};

        messageRetentionService.deleteExpiredMessages(mpc1, 10, 20);

        //the verifications are done in the Expectations block

    }

    @Test
    public void testDeleteExpiredDownloadedMessagesWithNegativeRetentionValue() {
        final String mpc1 = "mpc1";

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = -1;
        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, 10);

        new Verifications() {{
            userMessageLogDao.getDownloadedUserMessagesOlderThan(withAny(new Date()), anyString, null);
            times = 0;
        }};
    }

    @Test
    public void testDeleteExpiredNotDownloadedMessagesWithNegativeRetentionValue() {
        final String mpc1 = "mpc1";

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = -1;
        }};

        messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, 10);

        new Verifications() {{
            userMessageLogDao.getUndownloadedUserMessagesOlderThan(withAny(new Date()), anyString, null);
            times = 0;
        }};
    }

    @Test
    public void testDeleteExpiredDownloadedMessages() {
        String id1 = "1";
        String id2 = "2";
        final List<String> downloadedMessageIds = Arrays.asList(new String[]{id1, id2});
        final String mpc1 = "mpc1";
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = 10;

            userMessageLogDao.getDownloadedUserMessagesOlderThan(withAny(new Date()), mpc1, null);
            result = downloadedMessageIds;
        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, messagesDeleteLimit);

        new Verifications() {{
            List<JmsMessage> jmsMessages = new ArrayList<>();
            jmsManager.sendMessageToQueue(withCapture(jmsMessages), retentionMessageQueue); times = 2;
            assertEquals("Should have scheduled expired downloaded messages for deletion", downloadedMessageIds,
                    jmsMessages.stream().map(jmsMessage -> jmsMessage.getStringProperty(MessageConstants.MESSAGE_ID)).collect(Collectors.toList()));
        }};
    }

    @Test
    public void testDeleteExpiredNotDownloadedMessages() {
        String id1 = "1";
        String id2 = "2";
        final List<String> downloadedMessageIds = Arrays.asList(new String[]{id1, id2});
        final String mpc1 = "mpc1";
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = 10;

            userMessageLogDao.getUndownloadedUserMessagesOlderThan(withAny(new Date()), mpc1, null);
            result = downloadedMessageIds;
        }};

        messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, messagesDeleteLimit);

        new Verifications() {{
            List<JmsMessage> jmsMessages = new ArrayList<>();
            jmsManager.sendMessageToQueue(withCapture(jmsMessages), retentionMessageQueue); times = 2;
            assertEquals("Should have scheduled expired not downloaded messages for deletion", downloadedMessageIds,
                    jmsMessages.stream().map(jmsMessage -> jmsMessage.getStringProperty(MessageConstants.MESSAGE_ID)).collect(Collectors.toList()));
        }};
    }

    @Test
    public void testGetRetentionValueWithValidRetentionValue() {
        final String propertyName = "retentionLimitProperty";

        new Expectations(messageRetentionService) {{
            domibusPropertyProvider.getIntegerDomainProperty(propertyName);
            result = 5;
        }};

        final Integer retentionValue = messageRetentionService.getRetentionValue(propertyName);
        Assert.assertEquals(retentionValue, Integer.valueOf(5));
    }
}
