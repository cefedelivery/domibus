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
 */
public class CategoryLogger extends LoggerWrapper implements Logger {

    private static final String FQCN = CategoryLogger.class.getName();

    protected MessageConverter messageConverter;
    protected String mdcPropertyPrefix;

    public CategoryLogger(Logger logger, MessageConverter messageConverter, String mdcPropertyPrefix) {
        super(logger, LoggerWrapper.class.getName());
        if (messageConverter == null) {
            throw new IllegalArgumentException("MessageConverter cannot be null");
        }
        this.messageConverter = messageConverter;
        this.mdcPropertyPrefix = mdcPropertyPrefix;
    }

    public void trace(Marker marker, MessageCode key, Object... args) {
        if (!logger.isTraceEnabled()) {
            return;
        }
        String translatedMsg = formatMessage(marker, key, args);

        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.TRACE_INT, translatedMsg, args, null);
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
            ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.DEBUG_INT, translatedMsg, args, null);
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
            ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.INFO_INT, translatedMsg, args, null);
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
            ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.WARN_INT, translatedMsg, args, null);
        } else {
            logger.warn(marker, translatedMsg, args);
        }
    }

    public void error(Marker marker, MessageCode key, Object... args) {
        if (!logger.isErrorEnabled()) {
            return;
        }
        String translatedMsg = formatMessage(marker, key, args);

        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.ERROR_INT, translatedMsg, args, null);
        } else {
            logger.error(marker, translatedMsg, args);
        }
    }

    public void error(Marker marker, MessageCode key, Throwable t, Object... args) {
        if (!logger.isErrorEnabled()) {
            return;
        }
        String translatedMsg = formatMessage(marker, key, args);

        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.ERROR_INT, translatedMsg, args, t);
        } else {
            logger.error(marker, translatedMsg, t);
        }
    }

    protected String formatMessage(Marker marker, MessageCode key, Object[] args) {
        return messageConverter.getMessage(marker, key, args);
    }

    public void putMDC(String key, String val) {
        MDC.put(getKeyValue(key), val);
    }

    public void removeMDC(String key) {
        MDC.remove(getKeyValue(key));
    }

    protected String getKeyValue(String key) {
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
