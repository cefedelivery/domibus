package eu.domibus.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.LogbackLoggingConfigurator;
import eu.domibus.web.rest.ro.LoggingLevelRO;
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

//    public static final String COMMAND_LOG_LEVEL = "LOG_LEVEL";
//    public static final String COMMAND_LOG_NAME = "LOG_NAME";

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

//    @Autowired
//    protected Topic clusterCommandTopic;
//
//    @Autowired
//    protected JMSManager jmsManager;

    @Autowired
    protected SignalService signalService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoggingLevel(final String name, final String level) {

        //get the level from the string value
        Level levelObj = toLevel(level);

        //get the logger context
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        loggerContext.getLogger(name).setLevel(levelObj);
        LOG.info("Setting log level: [{}] for name: [{}]", level, name);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void signalSetLoggingLevel(final String name, final String level) {

        try {
            // Sends a signal to all the servers from the cluster in order to trigger the reset of the logging config
//            jmsManager.sendMessageToTopic(JMSMessageBuilder.create()
//                    .property(Command.COMMAND, Command.LOGGING_SET_LEVEL)
//                    .property(COMMAND_LOG_NAME, name)
//                    .property(COMMAND_LOG_LEVEL, level)
//                    .build(), clusterCommandTopic);
            signalService.signalLoggingSetLevel(name, level);

        } catch (Exception e) {
            throw new LoggingException("Error while sending topic message for setting logging level", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LoggingEntry> getLoggingLevel(String loggerName, boolean showClasses) {

        List<LoggingEntry> result = new ArrayList<>();
        if (StringUtils.isBlank(loggerName)) {
            return result;
        }

        LOG.info("showing logging for name: [{}] including classes: [{}]", loggerName, showClasses);

        //getting the logger context and list of loggers
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<Logger> loggerList = loggerContext.getLoggerList();

        Predicate<Logger> nameStartsWithPredicate = p -> p.getName().startsWith(loggerName);
        Predicate<Logger> nameContainsPredicate = p -> p.getName().contains(loggerName);
        Predicate<Logger> isLoggerForClassPredicate = p -> addLoggerOfClass(p, showClasses);
        Predicate<Logger> fullPredicate = (nameStartsWithPredicate.or(nameContainsPredicate)).and(isLoggerForClassPredicate);

        //filter existing loggers which starts with name
        result = loggerList.stream().filter(fullPredicate).map(this::convertToLoggingLevel).
                collect(Collectors.toList());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public void resetLogging() {
        //we are re-using the same service used at context initialization
        final String logbackConfigurationFile = new LogbackLoggingConfigurator(domibusConfigurationService).getLoggingConfigurationFile();

        // assume SLF4J is bound to logback in the current environment
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            context.reset();
            configurator.doConfigure(logbackConfigurationFile);
        } catch (JoranException je) {
            // StatusPrinter will handle this
            throw new LoggingException("Error occurred while reset logging", je);
        } finally {
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void signalResetLogging() {

        try {
            // Sends a signal to all the servers from the cluster in order to trigger the reset of the logging config
//            jmsManager.sendMessageToTopic(JMSMessageBuilder.create()
//                    .property(Command.COMMAND, Command.LOGGING_RESET)
//                    .build(), clusterCommandTopic);
            signalService.signalLoggingReset();
        } catch (Exception e) {
            throw new LoggingException("Error while sending topic message for logging reset", e);
        }
    }


    /**
     * returning an object of type {@link Level} or null if the input string is not correct
     *
     * @param logLevel String value
     * @return
     */
    protected Level toLevel(String logLevel) {
        final LoggingException loggingException = new LoggingException("Not a known log level: [" + logLevel + "]");

        if (StringUtils.isBlank(logLevel)) {
            throw loggingException;
        }
        switch (logLevel.toUpperCase()) {
            case "ALL":
                return Level.ALL;
            case "TRACE":
                return Level.TRACE;
            case "DEBUG":
                return Level.DEBUG;
            case "INFO":
                return Level.INFO;
            case "WARN":
                return Level.WARN;
            case "ERROR":
                return Level.ERROR;
            case "OFF":
                return Level.OFF;

            default:
                throw loggingException;
        }
    }

    /**
     * It will check if the logger has children or not.
     * No children means is a logger of a class not package
     *
     * @param logger      logger object
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

    /**
     * Converts to custom POJO for showing logger levels on GUI
     * It uses always effectiveLvele as this is always not null
     *
     * @param logger
     * @return {@link LoggingLevelRO}
     */
    private LoggingEntry convertToLoggingLevel(final Logger logger) {
        if (logger == null) {
            return null;
        }

        LoggingEntry result = new LoggingEntry();
        result.setName(logger.getName());
        //get effective level always - is never null
        result.setLevel(logger.getEffectiveLevel().toString());

        return result;
    }
}
