package eu.domibus.logging.api;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.ext.LoggerWrapper;
import org.slf4j.spi.LocationAwareLogger;

import java.util.Map;
import java.util.Set;

/**
 * A custom SLF4J logger specialized in logging using message codes.
 * It uses custom {@link MDC} methods in order to add a prefix to each MDC key in order to differentiate the Domibus key from keys used by third parties
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public class CategoryLogger extends LoggerWrapper implements Logger {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryLogger.class);

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
        String formattedMessage = formatMessage(marker, key, args);

        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, formattedMessage, args, null);
        } else {
            logger.trace(marker, formattedMessage, args);
        }
    }

    public void debug(Marker marker, MessageCode key, Object... args) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        String formattedMessage = formatMessage(marker, key, args);

        logDebug(marker, formattedMessage, args);
    }

    protected void logDebug(Marker marker, String message, Object... args) {
        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, message, args, null);
        } else {
            logger.debug(marker, message, args);
        }
    }

    protected void logDebug(String message, Object... args) {
        logDebug(null, message, args);
    }

    public void info(Marker marker, MessageCode key, Object... args) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        String formattedMessage = formatMessage(marker, key, args);

        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, formattedMessage, args, null);
        } else {
            logger.info(marker, formattedMessage, args);
        }
    }

    public void warn(Marker marker, MessageCode key, Object... args) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        String formattedMessage = formatMessage(marker, key, args);

        if (instanceofLAL) {
            ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, formattedMessage, args, null);
        } else {
            logger.warn(marker, formattedMessage, args);
        }
    }

    public void error(Marker marker, MessageCode key, Object... args) {
        if (!logger.isErrorEnabled()) {
            return;
        }
        String formattedMessage = formatMessage(marker, key, args);

        logError(marker, formattedMessage, null, args);
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
        final String mdcKey = getMDCKey(key);
        MDC.put(mdcKey, val);
        LOG.debug("Added key [{}] with value [{}] to MDC", mdcKey, val);
    }

    public void removeMDC(String key) {
        final String mdcKey = getMDCKey(key);
        MDC.remove(mdcKey);
        LOG.debug("Removed key [{}] from MDC", mdcKey);
    }

    public String getMDC(String key) {
        return MDC.get(getMDCKey(key));
    }

    public String getMDCKey(String key) {
        String keyValue = key;
        if (StringUtils.isNotEmpty(mdcPropertyPrefix)) {
            keyValue = mdcPropertyPrefix + keyValue;
        }
        return keyValue;
    }

    public void clearCustomKeys() {
        if (mdcPropertyPrefix == null) {
            LOG.debug("No custom keys defined: mdcPropertyPrefix is empty");
            return;
        }

        final Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        if (copyOfContextMap == null) {
            LOG.debug("No MDC keys to clear");
            return;
        }
        final Set<String> keySet = copyOfContextMap.keySet();
        for (String key : keySet) {
            if (StringUtils.startsWith(key, mdcPropertyPrefix)) {
                MDC.remove(key);
                LOG.debug("Removed key [{}] from MDC", key);
            }
        }
    }

    public void clearAll() {
        MDC.clear();
        LOG.debug("Cleared MDC");
    }
}
