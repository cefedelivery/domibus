package eu.domibus.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.LoggingLevelResultRO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.reflect.FieldUtils.readField;

/**
 * Service class for setting and retrieving logging levels at runtime
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Service
public class LoggingServiceImpl implements LoggingService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LoggingServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public LoggingLevelResultRO setLoggingLevel(LoggingLevelRO loggingLevelRO) {
        final LoggingLevelResultRO loggingLevelResultRO = new LoggingLevelResultRO();
        final String strLevel = loggingLevelRO.getLevel();
        final String name = loggingLevelRO.getName();

        Level level = toLevel(strLevel);

        if (level == null) {
            LOG.error("Not a known log level: {}", strLevel);
            loggingLevelResultRO.setMessage("Error, not a known log level: " + strLevel);
            return loggingLevelResultRO;
        }

        String msg = "Setting log level: " + level + " for ";
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(level);
            LOG.info("Setting log level: {} for root}", level);
            msg += "root";
        } else {
            loggerContext.getLogger(name).setLevel(level);
            LOG.info("Setting log level: {} for package name: {}", level, name);
            msg += "package name: " + name;
        }
        loggingLevelResultRO.setMessage(msg);
        loggingLevelResultRO.setLevel(level.toString());
        loggingLevelResultRO.setName(name);

        return loggingLevelResultRO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LoggingLevelRO> getLoggingLevel(String name, boolean showClasses) {

        List<LoggingLevelRO> resultList = new ArrayList<>();
        if (StringUtils.isBlank(name)) {
            return resultList;
        }

        LOG.info("showing logging for={} including classes=", name, showClasses);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        List<Logger> loggerList = loggerContext.getLoggerList();

        Predicate<Logger> nameStartsWithPredicate = p -> p.getName().startsWith(name);
        Predicate<Logger> nameContainsPredicate = p -> p.getName().contains(name);
        Predicate<Logger> isLoggerForClassPredicate = p -> addLoggerOfClass(p, showClasses);
        Predicate<Logger> fullPredicate = (nameStartsWithPredicate.or(nameContainsPredicate)).and(isLoggerForClassPredicate);

        //filter existing loggers which starts with name
        return loggerList.stream().filter(fullPredicate).map(this::convertToLoggingLevel).
                collect(Collectors.toList());
    }


    /**
     * returning an object of type {@link Level} or null if the input string is not correct
     *
     * @param logLevel
     * @return
     */
    private Level toLevel(String logLevel) {
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

    /**
     * It will check if the logger has children or not.
     * No children means is a logger of a class not package
     *
     * @param logger
     * @param showClasses - true or false to show classes logger too
     * @return true/false if this logger should be added to the list
     */
    @SuppressWarnings(value = "unchecked")
    private boolean addLoggerOfClass(Logger logger, boolean showClasses) {
        if (showClasses) {
            //it doesn't matter, this logger will be added anyway
            return true;
        }

        List<Logger> childrenList = null;
        try {
            childrenList = (List<Logger>) readField(logger, "childrenList", true);
        } catch (IllegalAccessException e) {
            LOG.debug("Not able to read children for logger: {}", logger.getName());
        }

        return CollectionUtils.isNotEmpty(childrenList);
    }

    private LoggingLevelRO convertToLoggingLevel(final Logger logger) {
        if (logger == null) {
            return null;
        }

        LoggingLevelRO result = new LoggingLevelRO();
        result.setName(logger.getName());
        //get effective level always - is never null
        result.setLevel(logger.getEffectiveLevel().toString());

        return result;
    }
}
