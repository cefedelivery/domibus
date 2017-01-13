package eu.domibus.logging;

import eu.domibus.logging.api.MessageCode;
import eu.domibus.logging.api.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DefaultMessageConverter implements MessageConverter {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultMessageConverter.class);

    @Override
    public String getMessage(Marker marker, MessageCode messageCode, Object... args) {
        String message = null;
        try {
            message = MessageFormatter.arrayFormat(messageCode.getMessage(), args).getMessage();
        } catch (Exception throwable) {
            LOG.debug("Could not format the code [" + messageCode.getCode() + "]: message [" + messageCode.getMessage() + "] and arguments [" + Arrays.asList(args) + "]");
            message = messageCode.getMessage();
        }
        if (marker != null) {
            return "[" + marker + " - " + messageCode.getCode() + "] " + message;
        } else {
            return "[" + messageCode.getCode() + "] " + message;
        }
    }
}
