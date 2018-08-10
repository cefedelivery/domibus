package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.services.impl.ErrorLogCsvServiceImpl;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.web.rest.ro.ErrorLogRO;
import eu.domibus.web.rest.ro.ErrorLogResultRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    private static final String CSV_TITLE = "Error Signal Message Id, Msh Role, Message In Error Id, Error Code, Error Detail, Timestamp, Notified";

    @Tested
    ErrorLogResource errorLogResource;

    @Injectable
    ErrorLogDao errorLogDao;

    @Injectable
    DateUtil dateUtil;

    @Injectable
    DomainCoreConverter domainConverter;

    @Injectable
    ErrorLogCsvServiceImpl errorLogCsvServiceImpl;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;


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

    @Test
    public void testGetCsv() throws CsvException {
        // Given
        Date date = new Date();
        List<ErrorLogEntry> errorLogEntries = new ArrayList<>();
        ErrorLogEntry errorLogEntry = new ErrorLogEntry();
        errorLogEntry.setEntityId(1);
        final String errorDetailStr = "ErrorDetail";
        final String signalMessageIdStr = "SignalMessageId";
        final String refToMessageIdStr = "RefToMessageId";
        errorLogEntry.setErrorDetail(errorDetailStr);
        errorLogEntry.setErrorSignalMessageId(signalMessageIdStr);
        errorLogEntry.setMessageInErrorId(refToMessageIdStr);
        errorLogEntry.setErrorCode(ErrorCode.EBMS_0001);
        errorLogEntry.setMshRole(MSHRole.RECEIVING);
        errorLogEntry.setTimestamp(date);
        errorLogEntry.setNotified(date);
        errorLogEntries.add(errorLogEntry);

        List<ErrorLogRO> errorLogROEntries = new ArrayList<>();
        ErrorLogRO errorLogRO = new ErrorLogRO();
        errorLogRO.setErrorDetail(errorDetailStr);
        errorLogRO.setErrorSignalMessageId(signalMessageIdStr);
        errorLogRO.setMessageInErrorId(refToMessageIdStr);
        errorLogRO.setErrorCode(ErrorCode.EBMS_0001);
        errorLogRO.setMshRole(MSHRole.RECEIVING);
        errorLogRO.setTimestamp(date);
        errorLogRO.setNotified(date);
        errorLogROEntries.add(errorLogRO);
        new Expectations() {{
            domibusPropertyProvider.getProperty("domibus.ui.maximumcsvrows", anyString);
            result = ErrorLogCsvServiceImpl.MAX_NUMBER_OF_ENTRIES;
            errorLogDao.findPaged(anyInt,anyInt,anyString,anyBoolean, (HashMap<String, Object>) any);
            result = errorLogEntries;
            domainConverter.convert(errorLogEntries, ErrorLogRO.class);
            result = errorLogROEntries;
            errorLogCsvServiceImpl.exportToCSV(errorLogROEntries);
            result = CSV_TITLE +
                    signalMessageIdStr + "," + MSHRole.RECEIVING + "," + refToMessageIdStr + "," + ErrorCode.EBMS_0001.getErrorCodeName() + "," +
                    errorDetailStr + "," + date + "," + date+System.lineSeparator();
        }};

        // When
        final ResponseEntity<String> csv = errorLogResource.getCsv("timestamp", false,null, null, null, null, null,
                null, null, null, null);

        // Then
        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals(CSV_TITLE +
                signalMessageIdStr + "," + MSHRole.RECEIVING + "," + refToMessageIdStr + "," + ErrorCode.EBMS_0001.getErrorCodeName() + "," +
                errorDetailStr + "," + date + "," + date+System.lineSeparator(),
                csv.getBody());
    }
}
