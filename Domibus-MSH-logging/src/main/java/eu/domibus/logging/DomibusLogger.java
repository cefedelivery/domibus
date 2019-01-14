package eu.domibus.logging;

import eu.domibus.logging.api.CategoryLogger;
import eu.domibus.logging.api.MessageCode;
import eu.domibus.logging.api.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    public static final String MDC_PROPERTY_PREFIX = "d_";

    public static final Marker BUSINESS_MARKER = MarkerFactory.getMarker("BUSINESS");

    public static final Marker SECURITY_MARKER = MarkerFactory.getMarker("SECURITY");

    static boolean trace=false;

    static {
        String value = System.getenv("domibus.trace");
        if(value!=null){
            trace=Boolean.valueOf("value");
        }
    }

    static Map<String, List<String>> traceMap= Collections.synchronizedMap(new HashMap());

    @Override
    public void trace(Marker marker, MessageCode key, Object... args){
        saveSmartTrace(marker, key, args);
        super.trace(marker,key,args);
    }

    private void saveSmartTrace(Marker marker, MessageCode key, Object[] args) {
        if(trace){
            final String messageID = this.getMDC(DomibusLogger.MDC_MESSAGE_ID);
            if(messageID!=null){
                final List<String> traceList = traceMap.get(messageID);
                if(traceList==null){
                    traceMap.put(messageID,traceList);
                }
                final String formattedMessage = formatMessage(marker, key, args);
                traceList.add(formattedMessage);
            }
        }
    }

    public void removeMDC(String key) {
        if(trace){
            if(DomibusLogger.MDC_MESSAGE_ID.equalsIgnoreCase(key)){
                traceMap.remove(this.getMDC(DomibusLogger.MDC_MESSAGE_ID));
            }
        }
        super.removeMDC(key);
    }

    public void smartTrace(){
        if(trace){
            final List<String> traceList = traceMap.get(this.getMDC(DomibusLogger.MDC_MESSAGE_ID));
            if(traceList!=null){
                for (String trace : traceList) {
                    this.warn("Smart trace->map size:[{}] trace[{}]",traceMap.size(),trace);
                }
            }
        }
    }

    @Override
    public void debug(Marker marker, MessageCode key, Object... args){
        saveSmartTrace(marker, key, args);
        super.debug(marker,key,args);
    }

    @Override
    public void info(Marker marker, MessageCode key, Object... args){
        saveSmartTrace(marker, key, args);
        super.info(marker,key,args);
    }

    @Override
    public void warn(Marker marker, MessageCode key, Object... args){
        saveSmartTrace(marker, key, args);
        super.warn(marker,key,args);
    }

    @Override
    public void error(Marker marker, MessageCode key, Object... args){
        saveSmartTrace(marker, key, args);
        super.error(marker,key,args);
    }


    public DomibusLogger(Logger logger, MessageConverter messageConverter) {
        super(logger, DomibusLogger.class.getName(),messageConverter, MDC_PROPERTY_PREFIX);
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
