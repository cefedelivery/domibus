package eu.domibus.common.services.impl;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.util.CollectionUtil;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.api.security.AuthUtils;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@RunWith(JMockit.class)
public class MessageRetentionServiceTest {

    @Injectable
    private CollectionUtil collectionUtil;

    @Injectable
    private Properties domibusProperties;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private SignalMessageDao signalMessageDao;

    @Injectable
    private SignalMessageLogDao signalMessageLogDao;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    AuthUtils authUtils;

    @Tested
    MessageRetentionService messageRetentionService;

    @Test
    public void testDeleteExpiredMessages() throws Exception {
        final String mpc1 = "mpc1";
        final String mpc2 = "mpc2";
        final List<String> mpcs = Arrays.asList(new String[]{mpc1, mpc2});

        new Expectations(messageRetentionService) {{
            pModeProvider.getMpcURIList();
            result = mpcs;

            messageRetentionService.getRetentionValue(MessageRetentionService.DOWNLOADED_MESSAGES_DELETE_LIMIT_PROPERTY, MessageRetentionService.DEFAULT_DOWNLOADED_MESSAGES_DELETE_LIMIT);
            result = 10;

            messageRetentionService.getRetentionValue(MessageRetentionService.NOT_DOWNLOADED_MESSAGES_DELETE_LIMIT_PROPERTY, MessageRetentionService.DEFAULT_NOT_DOWNLOADED_MESSAGES_DELETE_LIMIT);
            result = 20;
        }};

        messageRetentionService.deleteExpiredMessages();

        new Verifications() {{
            messageRetentionService.deleteExpiredMessages(mpc1, 10, 20);
        }};
    }

    @Test
    public void testDeleteExpiredMessagesForMpc() throws Exception {
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
    public void testDeleteExpiredDownloadedMessagesWithNegativeRetentionValue() throws Exception {
        final String mpc1 = "mpc1";

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = -1;
        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, 10);

        new Verifications() {{
            userMessageLogDao.getDownloadedUserMessagesOlderThan(withAny(new Date()), anyString);
            times = 0;
        }};
    }

    @Test
    public void testDeleteExpiredNotDownloadedMessagesWithNegativeRetentionValue() throws Exception {
        final String mpc1 = "mpc1";

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = -1;
        }};

        messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, 10);

        new Verifications() {{
            userMessageLogDao.getUndownloadedUserMessagesOlderThan(withAny(new Date()), anyString);
            times = 0;
        }};
    }

    @Test
    public void testDeleteExpiredDownloadedMessages() throws Exception {
        String id1 = "1";
        String id2 = "2";
        final List<String> downloadedMessageIds = Arrays.asList(new String[]{id1, id2});
        final String mpc1 = "mpc1";
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = 10;

            userMessageLogDao.getDownloadedUserMessagesOlderThan(withAny(new Date()), mpc1);
            result = downloadedMessageIds;
        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, messagesDeleteLimit);

        new Verifications() {{
            messageRetentionService.delete(downloadedMessageIds, messagesDeleteLimit);
        }};
    }

    @Test
    public void testDeleteExpiredNotDownloadedMessages() throws Exception {
        String id1 = "1";
        String id2 = "2";
        final List<String> downloadedMessageIds = Arrays.asList(new String[]{id1, id2});
        final String mpc1 = "mpc1";
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = 10;

            userMessageLogDao.getUndownloadedUserMessagesOlderThan(withAny(new Date()), mpc1);
            result = downloadedMessageIds;
        }};

        messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, messagesDeleteLimit);

        new Verifications() {{
            messageRetentionService.delete(downloadedMessageIds, messagesDeleteLimit);
        }};
    }

    @Test
    public void testGetRetentionValueWithUndefinedRetentionValue() throws Exception {
        final String propertyName = "retentionLimitProperty";
        Integer defaultValue = 3;

        new Expectations(messageRetentionService) {{
            domibusProperties.getProperty(propertyName);
            result = null;
        }};

        final Integer retentionValue = messageRetentionService.getRetentionValue(propertyName, defaultValue);
        Assert.assertEquals(retentionValue, defaultValue);
    }

    @Test
    public void testGetRetentionValueWithInvalidRetentionValue() throws Exception {
        final String propertyName = "retentionLimitProperty";
        Integer defaultValue = 3;

        new Expectations(messageRetentionService) {{
            domibusProperties.getProperty(propertyName);
            result = "a2";
        }};

        final Integer retentionValue = messageRetentionService.getRetentionValue(propertyName, defaultValue);
        Assert.assertEquals(retentionValue, defaultValue);
    }

    @Test
    public void testGetRetentionValueWithValidRetentionValue() throws Exception {
        final String propertyName = "retentionLimitProperty";
        Integer defaultValue = 3;

        new Expectations(messageRetentionService) {{
            domibusProperties.getProperty(propertyName);
            result = "5";
        }};

        final Integer retentionValue = messageRetentionService.getRetentionValue(propertyName, defaultValue);
        Assert.assertEquals(retentionValue, Integer.valueOf(5));
    }
}
