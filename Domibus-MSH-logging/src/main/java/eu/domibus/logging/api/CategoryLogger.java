package eu.domibus.logging.api;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.ext.LoggerWrapper;
import org.slf4j.spi.LocationAwareLogger;

import java.util.Map;
import java.util.Set;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class CategoryLogger extends LoggerWrapper implements Logger {

    protected MessageConverter messageConverter;
    protected String mdcPropertyPrefix;
    protected String fqcn;

    public CategoryLogger(Logger logger, String fqcn, MessageConverter messageConverter, String mdcPropertyPrefix) {
        super(logger, LoggerWrapper.class.getName());
        if (messageConverter == null) {
            throw new IllegalArgumentException("MessageConverter cannot be null");
        }
        this.messageConverter = messageConverter;
        this.mdcPropertyPrefix = mdcPropertyPrefix;
        this.fqcn = fqcn;
    }

    public void trace(Marker marker, MessageCode key, Object... args) {
        if (!logger.isTraceEnabled()) {
            return;
        }
        String translatedMsg = formatMessage(marker, key, args);

        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, translatedMsg, args, null);
        } else {
            logger.trace(marker, translatedMsg, args);
        }
    }

    public void debug(Marker marker, MessageCode key, Object... args) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        String translatedMsg = formatMessage(marker, key, args);

        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, translatedMsg, args, null);
        } else {
            logger.debug(marker, translatedMsg, args);
        }
    }

    public void info(Marker marker, MessageCode key, Object... args) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        String translatedMsg = formatMessage(marker, key, args);

        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, translatedMsg, args, null);
        } else {
            logger.info(marker, translatedMsg, args);
        }
    }

    public void warn(Marker marker, MessageCode key, Object... args) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        String translatedMsg = formatMessage(marker, key, args);

        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, translatedMsg, args, null);
        } else {
            logger.warn(marker, translatedMsg, args);
        }
    }

    public void error(Marker marker, MessageCode key, Object... args) {
        if (!logger.isErrorEnabled()) {
            return;
        }
        String translatedMsg = formatMessage(marker, key, args);

        logError(marker, translatedMsg, null, args);
    }

    protected void logError(Marker marker, String message, Throwable t, Object[] args) {
        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, message, args, t);
        } else {
            logger.error(marker, message, args);
        }
    }

    protected String formatMessage(Marker marker, MessageCode key, Object[] args) {
        return messageConverter.getMessage(marker, key, args);
    }

    public void putMDC(String key, String val) {
        MDC.put(translateMDCKey(key), val);
    }

    public void removeMDC(String key) {
        MDC.remove(translateMDCKey(key));
    }

    public String translateMDCKey(String key) {
        String keyValue = key;
        if(StringUtils.isNotEmpty(mdcPropertyPrefix)) {
            keyValue = mdcPropertyPrefix + keyValue;
        }
        return keyValue;
    }

    public void clearCustomKeys() {
        if(mdcPropertyPrefix == null) {
            return;
        }

        final Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        if(copyOfContextMap == null) {
            return;
        }
        final Set<String> keySet = copyOfContextMap.keySet();
        for (String key : keySet) {
            if(StringUtils.startsWith(key, mdcPropertyPrefix)) {
                MDC.remove(key);
            }
        }
    }

    public void clearAll() {
        MDC.clear();
    }
}
