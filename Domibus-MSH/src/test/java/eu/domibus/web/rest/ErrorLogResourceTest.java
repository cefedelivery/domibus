package eu.domibus.web.rest;

import eu.domibus.api.util.DateUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.web.rest.ro.ErrorLogRO;
import eu.domibus.web.rest.ro.ErrorLogResultRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class ErrorLogResourceTest {

    @Tested
    ErrorLogResource errorLogResource;

    @Injectable
    ErrorLogDao errorLogDao;

    @Injectable
    DateUtil dateUtil;


    @Test
    public void testGetErrorLog() {
        // Given
        final List<ErrorLogEntry> resultList = new ArrayList<>();
        ErrorLogEntry errorLogEntry = new ErrorLogEntry();
        errorLogEntry.setEntityId(1);
        errorLogEntry.setErrorCode(ErrorCode.EBMS_0001);
        errorLogEntry.setErrorDetail("errorDetail");
        errorLogEntry.setErrorSignalMessageId("errorSignalMessageId");
        errorLogEntry.setMessageInErrorId("refToMessageId");
        errorLogEntry.setMshRole(MSHRole.RECEIVING);
        errorLogEntry.setNotified(new Date());
        errorLogEntry.setTimestamp(new Date());
        resultList.add(errorLogEntry);

        new Expectations() {{
            errorLogDao.countEntries((HashMap<String, Object>) any);
            result = 1;

            errorLogDao.findPaged(anyInt, anyInt, anyString, anyBoolean, (HashMap<String, Object>) any);
            result = resultList;
        }};

        // When
        ErrorLogResultRO errorLogResultRO = errorLogResource.getErrorLog(1, 10, "messageId", true,
                null, null, null, null,
                null, null, null, null, null);

        // Then
        Assert.assertNotNull(errorLogResultRO);
        Assert.assertEquals(new Integer(1), errorLogResultRO.getCount());
        Assert.assertEquals(1, errorLogResultRO.getErrorLogEntries().size());
        ErrorLogRO errorLogRO = errorLogResultRO.getErrorLogEntries().get(0);
        Assert.assertEquals(errorLogEntry.getErrorCode(), errorLogRO.getErrorCode());
        Assert.assertEquals(errorLogEntry.getErrorDetail(), errorLogRO.getErrorDetail());
        Assert.assertEquals(errorLogEntry.getErrorSignalMessageId(), errorLogRO.getErrorSignalMessageId());
        Assert.assertEquals(errorLogEntry.getMessageInErrorId(), errorLogRO.getMessageInErrorId());
        Assert.assertEquals(errorLogEntry.getMshRole(), errorLogRO.getMshRole());
        Assert.assertEquals(errorLogEntry.getNotified(), errorLogRO.getNotified());
        Assert.assertEquals(errorLogEntry.getTimestamp(), errorLogRO.getTimestamp());
    }
}
