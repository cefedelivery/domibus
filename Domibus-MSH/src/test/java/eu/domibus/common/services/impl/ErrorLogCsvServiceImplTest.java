package eu.domibus.common.services.impl;

import eu.domibus.api.csv.CsvException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.web.rest.ro.ErrorLogRO;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    public void testExportToCsv_EmptyList() throws CsvException {
        // Given
        // When
        final String exportToCSV = errorLogCsvService.exportToCSV(new ArrayList<>());

        // Then
        Assert.assertTrue(exportToCSV.trim().isEmpty());
    }

    @Test
    public void testExportToCsv_NullList() throws CsvException {
        // Given
        // When
        final String exportToCSV = errorLogCsvService.exportToCSV(null);

        // Then
        Assert.assertTrue(exportToCSV.trim().isEmpty());
    }

    @Test
    public void testExportToCsv() throws CsvException {
        // Given
        Date date = new Date();
        List<ErrorLogRO> errorLogROList = getErrorLogList(date);

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss'GMT'Z");
        ZonedDateTime d = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        String csvDate = d.format(f);

        // When
        final String exportToCSV = errorLogCsvService.exportToCSV(errorLogROList);

        // Then
        Assert.assertTrue(exportToCSV.contains("Error Signal Message Id,Msh Role,Message In Error Id,Error Code,Error Detail,Timestamp,Notified"));
        Assert.assertTrue(exportToCSV.contains("signalMessageId,RECEIVING,messageInErrorId,EBMS:0001,errorDetail,"+csvDate+","+csvDate));
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
