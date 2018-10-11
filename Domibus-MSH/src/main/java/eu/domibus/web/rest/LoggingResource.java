package eu.domibus.web.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import eu.domibus.core.cache.DomibusCacheServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * @author Catalin Enache
 * since 4.1
 */
@RestController
@RequestMapping(value = "/rest/logging")
public class LoggingResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheServiceImpl.class);

    @RequestMapping(value = "/loglevel/{loglevel}", method = RequestMethod.POST)
    public String loglevel(@PathVariable("loglevel") String strLevel, @RequestParam(value = "package") String packageName) {


        Level level = toLevel(strLevel);

        if (level == null) {
            LOG.error("Not a known loglevel: {}", strLevel);
            return "Error, not a known loglevel: " + strLevel;
        } else {
            return setLogLevel(level, packageName);
        }


    }

    public String setLogLevel(Level level, String packageName) {

        String retVal = "OK";
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (packageName.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(level);
            LOG.info("Setting log level: {} for root}", level, packageName);
        } else {
            loggerContext.getLogger(packageName).setLevel(level);
            LOG.info("Setting log level: {} for package name: {}", level, packageName);
        }
        return retVal;
    }

    /**
     * returning an object of type {@link Level} or null if the input string is not correct
     * @param logLevel
     * @return
     */
    protected Level toLevel(String logLevel) {
        Level result = null;
        if (logLevel.equalsIgnoreCase("ALL")) {
            result = Level.ALL;
        } else if (logLevel.equalsIgnoreCase("TRACE")) {
            result = Level.TRACE;
        } else if (logLevel.equalsIgnoreCase("DEBUG")) {
           result = Level.DEBUG;
        } else if (logLevel.equalsIgnoreCase("INFO")) {
           result = Level.INFO;
        } else if (logLevel.equalsIgnoreCase("WARN")) {
            result = Level.WARN;
        } else if (logLevel.equalsIgnoreCase("ERROR")) {
            result = Level.ERROR;
        } else if (logLevel.equalsIgnoreCase("OFF")) {
            result = Level.OFF;
        }

        return result;
    }

}
