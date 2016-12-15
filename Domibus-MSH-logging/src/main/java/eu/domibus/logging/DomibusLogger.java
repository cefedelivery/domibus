package eu.domibus.logging;

import eu.domibus.logging.api.CategoryLogger;
import eu.domibus.logging.api.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @author Cosmin Baciu
 */
public class DomibusLogger extends CategoryLogger {

    public static final Marker BUSINESS_MARKER = MarkerFactory.getMarker("BUSINESS");
    public static final Marker SECURITY_MARKER = MarkerFactory.getMarker("SECURITY");

    public DomibusLogger(Logger logger, MessageConverter messageConverter) {
        super(logger, messageConverter, "domibus_");
    }

    public DomibusLogger(Logger logger) {
        this(logger, new DefaultMessageConverter());
    }

    public void securityTrace(DomibusMessageCode key, Object... args) {
        trace(SECURITY_MARKER, key, args);
    }

    public void securityDebug(DomibusMessageCode key, Object... args) {
        debug(SECURITY_MARKER, key, args);
    }

    public void securityInfo(DomibusMessageCode key, Object... args) {
        info(SECURITY_MARKER, key, args);
    }

    public void securityWarn(DomibusMessageCode key, Object... args) {
        securityWarn(key, null, args);
    }

    public void securityWarn(DomibusMessageCode key, Throwable t, Object... args) {
        warn(SECURITY_MARKER, key, args);

        if (t != null) {
            final String message = formatMessage(SECURITY_MARKER, key, args);
            warn(message, t);
        }
    }

    public void securityError(DomibusMessageCode key, Object... args) {
        securityError(key, null, args);
    }

    public void securityError(DomibusMessageCode key, Throwable t, Object... args) {
        error(SECURITY_MARKER, key, args);

        if (t != null) {
            final String message = formatMessage(SECURITY_MARKER, key, args);
            error(message, t);
        }
    }


}
