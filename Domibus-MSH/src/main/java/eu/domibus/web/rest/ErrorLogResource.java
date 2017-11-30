package eu.domibus.web.rest;

import com.google.common.primitives.Ints;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.web.rest.ro.ErrorLogRO;
import eu.domibus.web.rest.ro.ErrorLogResultRO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/errorlogs")
public class ErrorLogResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogResource.class);

    private static final String MAXIMUM_NUMBER_CSV_ROWS = "domibus.ui.maximumcsvrows";

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    private DomainCoreConverter domainConverter;

    @RequestMapping(method = RequestMethod.GET)
    public ErrorLogResultRO getErrorLog(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "orderBy", required = false) String column,
            @RequestParam(value = "asc", defaultValue = "true") boolean asc,
            @RequestParam(value = "errorSignalMessageId", required = false) String errorSignalMessageId,
            @RequestParam(value = "mshRole", required = false) MSHRole mshRole,
            @RequestParam(value = "messageInErrorId", required = false) String messageInErrorId,
            @RequestParam(value = "errorCode", required = false) ErrorCode errorCode,
            @RequestParam(value = "errorDetail", required = false) String errorDetail,
            @RequestParam(value = "timestampFrom", required = false) String timestampFrom,
            @RequestParam(value = "timestampTo", required = false) String timestampTo,
            @RequestParam(value = "notifiedFrom", required = false) String notifiedFrom,
            @RequestParam(value = "notifiedTo", required = false) String notifiedTo) {

        LOGGER.debug("Getting error log");
        HashMap<String, Object> filters = createFilterMap(errorSignalMessageId, mshRole, messageInErrorId, errorCode, errorDetail, timestampFrom, timestampTo, notifiedFrom, notifiedTo);
        ErrorLogResultRO result = new ErrorLogResultRO();
        result.setFilter(filters);
        LOGGER.debug("using filters [{}]", filters);

        long entries = errorLogDao.countEntries(filters);
        LOGGER.debug("count [{}]", entries);
        result.setCount(Ints.checkedCast(entries));

        final List<ErrorLogEntry> errorLogEntries = errorLogDao.findPaged(pageSize * page, pageSize, column, asc, filters);
        result.setErrorLogEntries(convert(errorLogEntries));

        result.setErrorCodes(ErrorCode.values());
        result.setMshRoles(MSHRole.values());
        result.setPage(page);
        result.setPageSize(pageSize);

        return result;
    }

    @RequestMapping(path = "/csv", method = RequestMethod.GET)
    public ResponseEntity<String> getCsv(
            @RequestParam(value = "errorSignalMessageId", required = false) String errorSignalMessageId,
            @RequestParam(value = "mshRole", required = false) MSHRole mshRole,
            @RequestParam(value = "messageInErrorId", required = false) String messageInErrorId,
            @RequestParam(value = "errorCode", required = false) ErrorCode errorCode,
            @RequestParam(value = "errorDetail", required = false) String errorDetail,
            @RequestParam(value = "timestampFrom", required = false) String timestampFrom,
            @RequestParam(value = "timestampTo", required = false) String timestampTo,
            @RequestParam(value = "notifiedFrom", required = false) String notifiedFrom,
            @RequestParam(value = "notifiedTo", required = false) String notifiedTo) {
        ErrorLogResultRO result = new ErrorLogResultRO();

        HashMap<String, Object> filters = createFilterMap(errorSignalMessageId, mshRole, messageInErrorId, errorCode, errorDetail, timestampFrom, timestampTo, notifiedFrom, notifiedTo);
        result.setFilter(filters);

        int maxCSVrows = Integer.parseInt(domibusProperties.getProperty(MAXIMUM_NUMBER_CSV_ROWS,"10000"));

        final List<ErrorLogEntry> errorLogEntries = errorLogDao.findPaged(0, maxCSVrows, null, true, filters);
        final List<ErrorLogRO> errorLogROList = domainConverter.convert(errorLogEntries, ErrorLogRO.class);
        StringBuilder resultText = new StringBuilder(ErrorLogRO.csvTitle());
        for(ErrorLogRO errorLogEntry : errorLogROList) {
            resultText.append(errorLogEntry.toCsvString());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/ms-excel"))
                .header("Content-Disposition", "attachment; filename=errorlog_datatable.csv")
                .body(resultText.toString());
    }

    private HashMap<String, Object> createFilterMap(
                            String errorSignalMessageId,
                            MSHRole mshRole,
                            String messageInErrorId,
                            ErrorCode errorCode,
                            String errorDetail,
                            String timestampFrom,
                            String timestampTo,
                            String notifiedFrom,
                            String notifiedTo) {
        HashMap<String, Object> filters = new HashMap<>();
        filters.put("errorSignalMessageId", errorSignalMessageId);
        filters.put("mshRole", mshRole);
        filters.put("messageInErrorId", messageInErrorId);
        filters.put("errorCode", errorCode);
        filters.put("errorDetail", errorDetail);

        filters.put("timestampFrom", dateUtil.fromString(timestampFrom));
        filters.put("timestampTo", dateUtil.fromString(timestampTo));
        filters.put("notifiedFrom", dateUtil.fromString(notifiedFrom));
        filters.put("notifiedTo", dateUtil.fromString(notifiedTo));

        return filters;
    }

    protected List<ErrorLogRO> convert(List<ErrorLogEntry> errorLogEntries) {
        List<ErrorLogRO> result = new ArrayList<>();
        for (ErrorLogEntry errorLogEntry : errorLogEntries) {
            final ErrorLogRO errorLogRO = convert(errorLogEntry);
            if (errorLogRO != null) {
                result.add(errorLogRO);
            }
        }
        return result;
    }

    protected ErrorLogRO convert(ErrorLogEntry errorLogEntry) {
        if (errorLogEntry == null) {
            return null;
        }
        ErrorLogRO result = new ErrorLogRO();
        result.setTimestamp(errorLogEntry.getTimestamp());
        result.setNotified(errorLogEntry.getNotified());
        result.setErrorCode(errorLogEntry.getErrorCode());
        result.setMshRole(errorLogEntry.getMshRole());
        result.setErrorDetail(errorLogEntry.getErrorDetail());
        result.setErrorSignalMessageId(errorLogEntry.getErrorSignalMessageId());
        result.setMessageInErrorId(errorLogEntry.getMessageInErrorId());
        return result;
    }
}
