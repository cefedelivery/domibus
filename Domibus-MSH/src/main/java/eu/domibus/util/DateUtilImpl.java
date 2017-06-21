package eu.domibus.util;

import eu.domibus.api.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;


/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class DateUtilImpl implements DateUtil {

    @Override
    public Date fromString(String value) {
        Date result = null;

        if (StringUtils.isNotEmpty(value)) {
            if (StringUtils.isNumeric(value)) {
                result = fromNumber(Long.parseLong(value));
            } else {
                result = fromISO8601(value);
            }
        }

        return result;
    }

    public Timestamp fromNumber(Number value) {
        return new Timestamp(value.longValue());
    }

    public Timestamp fromISO8601(String value) {
        DateTime dateTime = new DateTime(value);
        return new Timestamp(dateTime.getMillis());
    }
}
