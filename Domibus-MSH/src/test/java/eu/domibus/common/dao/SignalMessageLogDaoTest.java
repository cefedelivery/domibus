package eu.domibus.common.dao;

import eu.domibus.common.MSHRole;
import eu.domibus.common.model.logging.SignalMessageLog;
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
public class SignalMessageLogDaoTest {

    @Injectable
    EntityManager em;

    @Tested
    SignalMessageLogDao signalMessageLogDao;

    @Test
    public void testFindByMessageId_IdMatch(final @Injectable TypedQuery<SignalMessageLog> query) {

        final String searchMessageId = "MESSAGE_ID1234";

        final SignalMessageLog dbSignalMessageLog = new SignalMessageLog();
        dbSignalMessageLog.setMessageId(searchMessageId);
        new Expectations() {{
            query.getSingleResult();
            result = dbSignalMessageLog;
        }};

        SignalMessageLog resultSignalMessageLog = signalMessageLogDao.findByMessageId(searchMessageId);
        Assert.assertEquals(searchMessageId, resultSignalMessageLog.getMessageId());
    }

    @Test
    public void testFindByMessageId_IdMisMatch(final @Injectable TypedQuery<SignalMessageLog> query) {

        final String searchMessageId = "MESSAGE_ID1234";
        final String dbMessageId = "message_ID1234";

        final SignalMessageLog dbSignalMessageLog = new SignalMessageLog();
        dbSignalMessageLog.setMessageId(dbMessageId);
        new Expectations() {{
            query.getSingleResult();
            result = dbSignalMessageLog;
        }};

        try {
            SignalMessageLog resultSignalMessageLog = signalMessageLogDao.findByMessageId(searchMessageId);
            Assert.fail("Expected NoResultException was not raised!!");
        } catch (Exception e) {
            Assert.assertTrue("Expecting NoResultException:", e instanceof NoResultException);
        }
    }

    @Test
    public void testFindByMessageId_NullCheck(final @Injectable TypedQuery<SignalMessageLog> query) {

        final SignalMessageLog dbSignalMessageLog = new SignalMessageLog();
        dbSignalMessageLog.setMessageId(null);

        new Expectations() {{
            query.getSingleResult();
            result = dbSignalMessageLog;
        }};

        try {
            SignalMessageLog resultSignalMessageLog = signalMessageLogDao.findByMessageId(null);
            Assert.assertNotNull("SignalMessageLog is expected to be empty, Not null.", resultSignalMessageLog);
            Assert.assertNull(resultSignalMessageLog.getMessageId());
        } catch (Exception e) {
            Assert.fail("No exception was expected. Nulls should be handled!");
        }
    }

    @Test
    public void testFindByMessageIdandMSH_IdMatch(final @Injectable TypedQuery<SignalMessageLog> query) {

        final String searchMessageId = "MESSAGE_ID1234";

        final SignalMessageLog dbSignalMessageLog = new SignalMessageLog();
        dbSignalMessageLog.setMessageId(searchMessageId);
        dbSignalMessageLog.setMshRole(MSHRole.SENDING);
        new Expectations() {{
            query.getSingleResult();
            result = dbSignalMessageLog;
        }};

        SignalMessageLog resultSignalMessageLog = signalMessageLogDao.findByMessageId(searchMessageId, MSHRole.SENDING);
        Assert.assertEquals(searchMessageId, resultSignalMessageLog.getMessageId());
    }

    @Test
    public void testFindByMessageIdandMSH_IdMisMatch(final @Injectable TypedQuery<SignalMessageLog> query) {

        final String searchMessageId = "MESSAGE_ID1234";
        final String dbMessageId = "message_ID1234";

        final SignalMessageLog dbSignalMessageLog = new SignalMessageLog();
        dbSignalMessageLog.setMessageId(dbMessageId);
        dbSignalMessageLog.setMshRole(MSHRole.RECEIVING);
        new Expectations() {{
            query.getSingleResult();
            result = dbSignalMessageLog;
        }};

        try {
            SignalMessageLog resultSignalMessageLog = signalMessageLogDao.findByMessageId(searchMessageId, MSHRole.RECEIVING);
            Assert.assertNull(resultSignalMessageLog);
        } catch (Exception e) {
            Assert.fail("Expected null in result of operation with no exception!!");
        }
    }

    @Test
    public void testFindByMessageIdandMSH_NullCheck(final @Injectable TypedQuery<SignalMessageLog> query) {

        new Expectations() {{
            query.getSingleResult();
            result = null;
        }};

        try {
            SignalMessageLog resultSignalMessageLog = signalMessageLogDao.findByMessageId(null, MSHRole.RECEIVING);
            Assert.assertNull(resultSignalMessageLog);
        } catch (Exception e) {
            Assert.fail("Expected null in result of operation with no exception!!");
        }
    }
}
