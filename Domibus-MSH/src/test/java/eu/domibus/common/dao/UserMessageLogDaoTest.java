package eu.domibus.common.dao;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.logging.UserMessageLog;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * @author Arun Raj
 * @since 3.3
 */
@RunWith(JMockit.class)
public class UserMessageLogDaoTest {

    @Injectable
    EntityManager em;

    @Tested
    UserMessageLogDao userMessageLogDao;

    @Test
    public void testFindByMessageId_IDMatch(final @Injectable TypedQuery<UserMessageLog> query) {
        final String searchMessageID = "MESSAGE_ID1234";

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageId(searchMessageID);
        userMessageLog.setMessageStatus(MessageStatus.ACKNOWLEDGED);

        new Expectations() {{
            query.getSingleResult();
            result = userMessageLog;
        }};

        UserMessageLog resultUserMessageLog = userMessageLogDao.findByMessageId(searchMessageID);
        Assert.assertEquals(searchMessageID, resultUserMessageLog.getMessageId());
    }

    @Test
    public void testGetMessageStatus_IDMatch(final @Injectable TypedQuery<UserMessageLog> query) {
        final String searchMessageID = "MESSAGE_ID1234";

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageId(searchMessageID);
        userMessageLog.setMessageStatus(MessageStatus.ACKNOWLEDGED);

        new Expectations() {{
            query.getSingleResult();
            result = userMessageLog;
        }};

        Assert.assertEquals(MessageStatus.ACKNOWLEDGED, userMessageLogDao.getMessageStatus(searchMessageID));
    }

    @Test
    public void testFindByMessageId_IDMismatch(final @Injectable TypedQuery<UserMessageLog> query) {

        final String searchMessageID = "MESSAGE_ID1234";
        final String dbMessageID = "message_ID1234";

        final UserMessageLog dbUserMessageLog = new UserMessageLog();
        dbUserMessageLog.setMessageId(dbMessageID);
        dbUserMessageLog.setMessageStatus(MessageStatus.ACKNOWLEDGED);

        new Expectations() {{
            query.getSingleResult();
            result = dbUserMessageLog;
        }};

        try {
            userMessageLogDao.findByMessageId(searchMessageID);
            Assert.fail("No Result Exception was expected!!");
        } catch (Exception e) {
            Assert.assertTrue("Expecting NoResultException", e instanceof NoResultException);
        }
    }

    @Test
    public void testFindByMessageId_NullCheck(final @Injectable TypedQuery<UserMessageLog> query) {

        final UserMessageLog dbUserMessageLog = new UserMessageLog();
        dbUserMessageLog.setMessageId(null);

        new Expectations() {{
            query.getSingleResult();
            result = dbUserMessageLog;
        }};

        try {
            UserMessageLog resultUserMessageLog = userMessageLogDao.findByMessageId(null);
            Assert.assertNotNull("UserMessageLog is expected to be empty, Not null.", resultUserMessageLog);
            Assert.assertNull(resultUserMessageLog.getMessageId());
        } catch (Exception e) {
            Assert.fail("No exception was expected. Nulls should be handled!");
        }
    }

    @Test
    public void testGetMessageStatus_IDMismatch(final @Injectable TypedQuery<UserMessageLog> query) {

        final String searchMessageID = "MESSAGE_ID1234";
        final String dbMessageID = "message_ID1234";

        final UserMessageLog dbUserMessageLog = new UserMessageLog();
        dbUserMessageLog.setMessageId(dbMessageID);
        dbUserMessageLog.setMessageStatus(MessageStatus.ACKNOWLEDGED);

        new Expectations() {{
            query.getSingleResult();
            result = dbUserMessageLog;
        }};

        Assert.assertEquals(MessageStatus.NOT_FOUND, userMessageLogDao.getMessageStatus(searchMessageID));
    }

    @Test
    public void testFindByMessageIdandMSH_IDMatch(final @Injectable TypedQuery<UserMessageLog> query) {
        final String searchMessageID = "MESSAGE_ID1234";

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageId(searchMessageID);

        new Expectations() {{
            query.getSingleResult();
            result = userMessageLog;
        }};

        UserMessageLog resultUserMessageLog = userMessageLogDao.findByMessageId(searchMessageID, MSHRole.SENDING);
        Assert.assertEquals(searchMessageID, resultUserMessageLog.getMessageId());
    }

    @Test
    public void testFindByMessageIdandMSH_IDMismatch(final @Injectable TypedQuery<UserMessageLog> query) {

        final String searchMessageID = "MESSAGE_ID1234";
        final String dbMessageID = "message_ID1234";

        final UserMessageLog dbUserMessageLog = new UserMessageLog();
        dbUserMessageLog.setMessageId(dbMessageID);

        new Expectations() {{
            query.getSingleResult();
            result = dbUserMessageLog;
        }};

        UserMessageLog resultUserMessageLog = userMessageLogDao.findByMessageId(searchMessageID, MSHRole.RECEIVING);
        Assert.assertNull(resultUserMessageLog);
    }


    @Test
    public void testFindByMessageIdandMSH_NullCheck(final @Injectable TypedQuery<UserMessageLog> query) {

        new Expectations() {{
            query.getSingleResult();
            result = null;
        }};

        try {
            UserMessageLog resultUserMessageLog = userMessageLogDao.findByMessageId(null, null);
            Assert.assertNull(resultUserMessageLog);
        } catch (Exception e) {

            Assert.fail("No exception was expected. Nulls should be handled!");
        }
    }

}
