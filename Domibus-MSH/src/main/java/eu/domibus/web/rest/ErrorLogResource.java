package eu.domibus.web.rest;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.web.rest.ro.ErrorLogRO;
import eu.domibus.web.rest.ro.ErrorLogResultRO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/errorlogs")
public class ErrorLogResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogResource.class);

    @RequestMapping(method = RequestMethod.GET)
    public ErrorLogResultRO getErrorLog() throws Exception {
        LOGGER.info("-------------errorLog");
        ErrorLogResultRO result = new ErrorLogResultRO();

        List<ErrorLogRO> errorLogEntries = new ArrayList<>();
        for (int i=0; i < 100; i++) {
            ErrorLogRO entry1 = new ErrorLogRO();
            entry1.setErrorCode("DOM_00" + i);
            entry1.setErrorDetail("Error occurred while calling the backend " + i);
            entry1.setErrorSignalMessageId("signal id" + i);
            entry1.setMessageInErrorId("messageIn error" + i);
            entry1.setMshRole("SENDING");
            entry1.setNotified(new Timestamp(System.currentTimeMillis()));
            entry1.setTimestamp(new Timestamp(System.currentTimeMillis()));
            errorLogEntries.add(entry1);
        }

        result.setErrorLogEntries(errorLogEntries);
        result.setCount(25);
        result.setErrorCodes(ErrorCode.values());
        result.setMshRoles(MSHRole.values());
        return result;
    }
}
