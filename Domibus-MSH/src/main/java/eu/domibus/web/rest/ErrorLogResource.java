package eu.domibus.web.rest;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.web.rest.ro.ErrorLogRO;
import eu.domibus.web.rest.ro.ErrorLogResultRO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/errorlogs")
public class ErrorLogResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogResource.class);

    @Autowired
    private ErrorLogDao errorLogDao;

    @RequestMapping(method = RequestMethod.GET)
    public ErrorLogResultRO getErrorLog(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int size,
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

        LOGGER.info("-------------errorLog");
        ErrorLogResultRO result = new ErrorLogResultRO();

//        List<ErrorLogRO> errorLogEntries = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            ErrorLogRO entry1 = new ErrorLogRO();
//            entry1.setErrorCode("DOM_00" + i);
//            entry1.setErrorDetail("Error occurred while calling the backend " + i);
//            entry1.setErrorSignalMessageId("signal id" + i);
//            entry1.setMessageInErrorId("messageIn error" + i);
//            entry1.setMshRole("SENDING");
//            entry1.setNotified(new Timestamp(System.currentTimeMillis()));
//            entry1.setTimestamp(new Timestamp(System.currentTimeMillis()));
//            errorLogEntries.add(entry1);
//        }




        HashMap<String, Object> filters = new HashMap<>();
        filters.put("errorSignalMessageId", errorSignalMessageId);
        filters.put("mshRole", mshRole);
        filters.put("messageInErrorId", messageInErrorId);
        filters.put("errorCode", errorCode);
        filters.put("errorDetail", errorDetail);
        filters.put("timestampFrom", timestampFrom);
        filters.put("timestampTo", timestampTo);
        filters.put("notifiedFrom", notifiedFrom);
        filters.put("notifiedTo", notifiedTo);
        result.setFilter(filters);

        long entries = errorLogDao.countEntries(filters);
        long pages = entries / size;
        if (entries % size != 0) {
            pages++;
        }
        int begin = Math.max(1, page - 5);
        long end = Math.min(begin + 10, pages);

        result.setCount(Long.valueOf(entries).intValue());

        ModelAndView model = new ModelAndView();
//        model.addObject("errorSignalMessageId", errorSignalMessageId);
//        model.addObject("mshRole", mshRole);
//        model.addObject("messageInErrorId", messageInErrorId);
//        model.addObject("errorCode", errorCode);
//        model.addObject("errorDetail", errorDetail);
//        model.addObject("timestampFrom", timestampFrom);
//        model.addObject("timestampTo", timestampTo);
//        model.addObject("notifiedFrom", notifiedFrom);
//        model.addObject("notifiedTo", notifiedTo);

//        model.addObject("page", page);
        //TODO pageSize should be sent to the UI and binded
        model.addObject("size", size);
//        model.addObject("pages", pages);
        //TODO column and asc should be sent as parameters
        model.addObject("column", column);
        model.addObject("asc", asc);
//        model.addObject("beginIndex", begin);
//        model.addObject("endIndex", end);
        if (page <= pages) {
            final List<ErrorLogEntry> errorLogEntries = errorLogDao.findPaged(size * (page/* - 1*/), size, column, asc, filters);
            result.setErrorLogEntries(convert(errorLogEntries));
        }

        result.setErrorCodes(ErrorCode.values());
        result.setMshRoles(MSHRole.values());
        result.setPage(page);
        result.setPageSize(size);

        return result;
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
        if(errorLogEntry == null) {
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
