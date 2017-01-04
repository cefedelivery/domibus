package eu.domibus.logging;

import eu.domibus.logging.api.CategoryLogger;
import eu.domibus.logging.api.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusLogger extends CategoryLogger {

    public static final String MDC_USER = "user";
    public static final String MDC_MESSAGE_ID = "messageId";

    public static final Marker BUSINESS_MARKER = MarkerFactory.getMarker("BUSINESS");
    public static final Marker SECURITY_MARKER = MarkerFactory.getMarker("SECURITY");

    private static final Marker LOGGED_MARKER = MarkerFactory.getMarker("LOGGED_MARKER");

    public DomibusLogger(Logger logger, MessageConverter messageConverter) {
        super(logger, DomibusLogger.class.getName(),messageConverter, "d_");
    }

    public DomibusLogger(Logger logger) {
        this(logger, new DefaultMessageConverter());
    }

    public void businessTrace(DomibusMessageCode key, Object... args) {
        trace(BUSINESS_MARKER, key, args);
    }

    public void businessDebug(DomibusMessageCode key, Object... args) {
        debug(BUSINESS_MARKER, key, args);
    }

    public void businessInfo(DomibusMessageCode key, Object... args) {
        info(BUSINESS_MARKER, key, args);
    }

    public void businessWarn(DomibusMessageCode key, Object... args) {
        businessWarn(key, null, args);
    }

    public void businessWarn(DomibusMessageCode key, Throwable t, Object... args) {
        markerWarn(BUSINESS_MARKER, key, t, args);
    }

    public void businessError(DomibusMessageCode key, Object... args) {
        businessError(key, null, args);
    }

    public void businessError(DomibusMessageCode key, Throwable t, Object... args) {
        markerError(BUSINESS_MARKER, key, t, args);
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
        markerWarn(SECURITY_MARKER, key, t, args);
    }

    public void securityError(DomibusMessageCode key, Object... args) {
        securityError(key, null, args);
    }

    public void securityError(DomibusMessageCode key, Throwable t, Object... args) {
        markerError(SECURITY_MARKER, key, t, args);
    }

    protected void markerWarn(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        warn(marker, key, args);

        if (t != null) {
            final String message = formatMessage(marker, key, args);
            warn(message, t);
        }
    }

    protected void markerError(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        if (t != null) {
            final String message = formatMessage(marker, key, args);
            logError(null, message, t, args);
            marker.add(LOGGED_MARKER);
        }

        error(marker, key, args);
    }


}
