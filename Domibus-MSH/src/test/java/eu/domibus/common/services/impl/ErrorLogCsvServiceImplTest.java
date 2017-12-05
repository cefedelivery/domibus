package eu.domibus.common.services.impl;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.web.rest.ro.ErrorLogRO;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@RunWith(JMockit.class)
public class ErrorLogCsvServiceImplTest {

    @Tested
    ErrorLogCsvServiceImpl errorLogCsvService;

    @Test
    public void testExportToCsv_EmptyList() throws EbMS3Exception {
        // Given
        // When
        final String exportToCSV = errorLogCsvService.exportToCSV(new ArrayList<>());

        // Then
        Assert.assertTrue(exportToCSV.isEmpty());
    }

    @Test
    public void testExportToCsv_NullList() throws EbMS3Exception {
        // Given
        // When
        final String exportToCSV = errorLogCsvService.exportToCSV(null);

        // Then
        Assert.assertTrue(exportToCSV.isEmpty());
    }

    @Test
    public void testExportToCsv() throws EbMS3Exception {
        Date date = new Date();
        List<ErrorLogRO> errorLogROList = getErrorLogList(date);
        final String exportToCSV = errorLogCsvService.exportToCSV(errorLogROList);
    }

    private List<ErrorLogRO> getErrorLogList(Date date) {
        List<ErrorLogRO> result = new ArrayList<>();
        ErrorLogRO errorLogRO = new ErrorLogRO();
        errorLogRO.setErrorCode(ErrorCode.EBMS_0001);
        errorLogRO.setErrorDetail("errorDetail");
        errorLogRO.setErrorSignalMessageId("signalMessageId");
        errorLogRO.setMessageInErrorId("messageInErrorId");
        errorLogRO.setMshRole(MSHRole.RECEIVING);
        errorLogRO.setNotified(date);
        errorLogRO.setTimestamp(date);
        result.add(errorLogRO);
        return result;
    }
}
