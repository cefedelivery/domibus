package eu.domibus.ebms3.common.model;

import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class is responsible for the generation of timestamps in an ebMS3 conformant way.
 *
 * @author Christian Koch, Stefan Mueller
 */
public class TimestampDateFormatter {

    @Autowired
    private SimpleDateFormat xmlDateTimeFormat;


    public String generateTimestamp() {
        return generateTimestamp(new Date());
    }

    public String generateTimestamp(Date timestamp) {
        this.xmlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return this.xmlDateTimeFormat.format(timestamp);
    }
}
