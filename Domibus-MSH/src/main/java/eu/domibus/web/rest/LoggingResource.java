package eu.domibus.web.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import eu.domibus.core.cache.DomibusCacheServiceImpl;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.LoggingLevelResultRO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Catalin Enache
 * since 4.1
 */
@RestController
@RequestMapping(value = "/rest/logging")
public class LoggingResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheServiceImpl.class);

    @Autowired
    private DomainCoreConverter domainConverter;

    @PostMapping(value = "/loglevel")
    public ResponseEntity<List<LoggingLevelResultRO>> setLogLevel(@RequestBody List<LoggingLevelRO> loggingLevelROS) {

        final List<LoggingLevelResultRO> loggingLevelResultROS = loggingLevelROS.stream().map(this::setLoggingLevel).collect(Collectors.toList());

        return ResponseEntity.ok().body(loggingLevelResultROS);

    }

    @GetMapping(value = "/loglevel")
    public ResponseEntity<List<LoggingLevelRO>> getLogLevel(@RequestParam(value = "name", defaultValue = "eu.domibus", required = false) String name,
                                                            @RequestParam(value="showClasses", required = false, defaultValue = "false") boolean showClasses) {

        final List<LoggingLevelRO> loggingLevelResultROS = getLoggingLevel(name, showClasses);

        return ResponseEntity.ok().body(loggingLevelResultROS);

    }

    /**
     * set the logging level for the given packages
     * @param loggingLevelRO
     * @return
     */
    protected LoggingLevelResultRO setLoggingLevel(LoggingLevelRO loggingLevelRO) {
        final LoggingLevelResultRO loggingLevelResultRO = new LoggingLevelResultRO();
        final String strLevel = loggingLevelRO.getLevel();
        final String packageName = loggingLevelRO.getName();

        Level level = toLevel(strLevel);

        if (level == null) {
            LOG.error("Not a known log level: {}", strLevel);
            loggingLevelResultRO.setMessage("Error, not a known log level: " + strLevel);
            return loggingLevelResultRO;
        }

        String msg = "Setting log level: " + level + " for ";
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (packageName.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(level);
            LOG.info("Setting log level: {} for root}", level);
            msg += "root";
        } else {
            loggerContext.getLogger(packageName).setLevel(level);
            LOG.info("Setting log level: {} for package name: {}", level, packageName);
            msg += "package name: " + packageName;
        }
        loggingLevelResultRO.setMessage(msg);
        loggingLevelResultRO.setLevel(level.toString());
        loggingLevelResultRO.setName(packageName);

        return loggingLevelResultRO;
    }

    /**
     * set the logging level for the given packages
     * @param name - package or full Class name
     * @return
     */
    protected List<LoggingLevelRO> getLoggingLevel(final String name, final boolean includeClasses) {

        List<LoggingLevelRO> resultList = new ArrayList<>();
        if (StringUtils.isBlank(name)) {
            return resultList;
        }


        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        List<Logger> loggerList =  loggerContext.getLoggerList();

        return loggerList.stream().filter(logger -> logger.getName().startsWith(name)).map(logger -> convertToLoggingLevel(logger)).
                collect(Collectors.toList());
    }

    /**
     * returning an object of type {@link Level} or null if the input string is not correct
     *
     * @param logLevel
     * @return
     */
    protected Level toLevel(String logLevel) {
        Level result = null;
        if ("ALL".equalsIgnoreCase(logLevel)) {
            result = Level.ALL;
        } else if ("TRACE".equalsIgnoreCase(logLevel)) {
            result = Level.TRACE;
        } else if ("DEBUG".equalsIgnoreCase(logLevel)) {
            result = Level.DEBUG;
        } else if ("INFO".equalsIgnoreCase(logLevel)) {
            result = Level.INFO;
        } else if ("WARN".equalsIgnoreCase(logLevel)) {
            result = Level.WARN;
        } else if ("ERROR".equalsIgnoreCase(logLevel)) {
            result = Level.ERROR;
        } else if ("OFF".equalsIgnoreCase(logLevel)) {
            result = Level.OFF;
        }

        return result;
    }

    private LoggingLevelRO convertToLoggingLevel(final Logger logger) {
        if (logger == null) {
            return null;
        }

        LoggingLevelRO result = new LoggingLevelRO();
        result.setName(logger.getName());
        result.setLevel(logger.getLevel() != null ? logger.getLevel().toString() : null);

        return result;
    }
}
