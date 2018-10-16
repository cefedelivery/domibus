package eu.domibus.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.LoggingLevelResponseRO;
import eu.domibus.web.rest.ro.LoggingLevelResultRO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    DomainCoreConverter domainConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public LoggingLevelResponseRO setLoggingLevel(LoggingLevelRO loggingLevelRO) {
        final LoggingLevelResponseRO resultRO = domainConverter.convert(loggingLevelRO, LoggingLevelResponseRO.class);

        resultRO.setResult(LoggingLevelResponseRO.Result.SUCCESS);
        final String strLevel = loggingLevelRO.getLevel();
        final String name = loggingLevelRO.getName();


        Level level = toLevel(strLevel);

        if (level == null) {
            LOG.error("Not a known log level: {}", strLevel);
            resultRO.setMessage("Error. Not a known log level: " + strLevel);
            resultRO.setResult(LoggingLevelResponseRO.Result.ERROR);
            return resultRO;
        }

        String msg = "Success. Log level set to: " + level + " for ";
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(level);
            LOG.info("Setting log level: {} for root", level);
            msg += "root";
        } else {
            loggerContext.getLogger(name).setLevel(level);
            LOG.info("Setting log level: {} for package name: {}", level, name);
            msg += "package name: " + name;
        }
        resultRO.setResult(LoggingLevelResponseRO.Result.SUCCESS);
        resultRO.setMessage(msg);
        resultRO.setLevel(level.toString());


        return resultRO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoggingLevelResultRO getLoggingLevel(String loggerName, boolean showClasses, int page, int pageSize) {

        final LoggingLevelResultRO resultRO = new LoggingLevelResultRO();

        List<LoggingLevelRO> loggingEntries = new ArrayList<>();
        if (StringUtils.isBlank(loggerName)) {
            resultRO.setLoggingEntries(loggingEntries);
            return resultRO;
        }

        LOG.info("showing logging for name: {} including classes: {}", loggerName, showClasses);

        //getting the logger context and list of loggers
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<Logger> loggerList = loggerContext.getLoggerList();

        Predicate<Logger> nameStartsWithPredicate = p -> p.getName().startsWith(loggerName);
        Predicate<Logger> nameContainsPredicate = p -> p.getName().contains(loggerName);
        Predicate<Logger> isLoggerForClassPredicate = p -> addLoggerOfClass(p, showClasses);
        Predicate<Logger> fullPredicate = (nameStartsWithPredicate.or(nameContainsPredicate)).and(isLoggerForClassPredicate);

        //filter existing loggers which starts with name
        List<LoggingLevelRO> tmp = loggerList.stream().filter(fullPredicate).map(this::convertToLoggingLevel).
                collect(Collectors.toList());
        int count = tmp.size();
        int fromIndex = pageSize * page;
        int toIndex = fromIndex + pageSize;
        if (toIndex > count) {
            toIndex = count;
        }

        resultRO.setCount(count);
        resultRO.setLoggingEntries(tmp.subList(fromIndex, toIndex));
        return resultRO;

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
