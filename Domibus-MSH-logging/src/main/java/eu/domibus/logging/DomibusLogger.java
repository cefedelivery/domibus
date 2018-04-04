package eu.domibus.logging;

import eu.domibus.logging.api.CategoryLogger;
import eu.domibus.logging.api.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Map;


/**
 * A custom SLF4J logger specialized in logging using business and security events using specific Domibus message codes
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusLogger extends CategoryLogger {

    public static final String MDC_USER = "user";
    public static final String MDC_MESSAGE_ID = "messageId";
    public static final String MDC_DOMAIN = "domain";

    public static final Marker BUSINESS_MARKER = MarkerFactory.getMarker("BUSINESS");
    public static final Marker SECURITY_MARKER = MarkerFactory.getMarker("SECURITY");

    public DomibusLogger(Logger logger, MessageConverter messageConverter) {
        super(logger, DomibusLogger.class.getName(),messageConverter, "d_");
    }

    public DomibusLogger(Logger logger) {
        this(logger, new DefaultMessageConverter());
    }

    public void businessTrace(DomibusMessageCode key, Object... args) {
        markerTrace(BUSINESS_MARKER, key, null, args);
    }

    public void businessDebug(DomibusMessageCode key, Object... args) {
        markerDebug(BUSINESS_MARKER, key, null, args);
    }

    public void businessInfo(DomibusMessageCode key, Object... args) {
        markerInfo(BUSINESS_MARKER, key, null, args);
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
        markerTrace(SECURITY_MARKER, key, null, args);
    }

    public void securityDebug(DomibusMessageCode key, Object... args) {
        markerDebug(SECURITY_MARKER, key, null, args);
    }

    public void securityInfo(DomibusMessageCode key, Object... args) {
        markerInfo(SECURITY_MARKER, key, null, args);
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

    protected void markerTrace(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        // log with no marker and stacktrace (if there is one)
        trace(null, key, t, args);

        //log with marker and without stacktrace
        trace(marker, key, args);
    }

    protected void markerDebug(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        // log with no marker and stacktrace (if there is one)
        debug(null, key, t, args);

        //log with marker and without stacktrace
        debug(marker, key, args);
    }

    protected void markerInfo(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        // log with no marker and stacktrace (if there is one)
        info(null, key, t, args);

        //log with marker and without stacktrace
        info(marker, key, args);
    }

    protected void markerWarn(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        // log with no marker and stacktrace (if there is one)
        warn(null, key, t, args);

        //log with marker and without stacktrace
        warn(marker, key, args);
    }

    protected void markerError(Marker marker, DomibusMessageCode key, Throwable t, Object... args) {
        // log with no marker and stacktrace (if there is one)
        error(null, key, t, args);

        //log with marker and without stacktrace
        error(marker, key, args);
    }

    public Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }
}
