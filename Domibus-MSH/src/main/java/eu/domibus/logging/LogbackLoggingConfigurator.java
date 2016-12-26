package eu.domibus.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import eu.domibus.api.logging.LoggingConfigurator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by Cosmin Baciu on 12-Oct-16.
 * @since 3.3
 */
@Component
public class LogbackLoggingConfigurator implements LoggingConfigurator {

    private static final String DOMIBUS_CONFIG_LOCATION = "domibus.config.location";
    private static final String DEFAULT_LOGBACK_FILE_NAME = "logback.xml";
    private static final String LOGBACK_CONFIGURATION_FILE_PARAM = "logback.configurationFile";

    private static final Logger LOG = LoggerFactory.getLogger(LogbackLoggingConfigurator.class);

    @Override
    public void configureLogging() {
        String logbackConfigurationFile = getDefaultLogbackConfigurationFile();
        String customLogbackConfigurationFile = System.getProperty(LOGBACK_CONFIGURATION_FILE_PARAM);
        if (StringUtils.isNotEmpty(customLogbackConfigurationFile)) {
            LOG.info("Found custom logback configuration file: [" + customLogbackConfigurationFile + "]");
            logbackConfigurationFile = customLogbackConfigurationFile;
        }
        configureLogging(logbackConfigurationFile);
    }

    @Override
    public void configureLogging(String logbackConfigurationFile) {
        if (StringUtils.isEmpty(logbackConfigurationFile)) {
            LOG.warn("Could not configure logging: the provided configuration file is empty");
            return;
        }

        LOG.info("Using the logback configuration file from [" + logbackConfigurationFile + "]");

        if (!new File(logbackConfigurationFile).exists()) {
            LOG.warn("Could not configure logging: the file [" + logbackConfigurationFile + "] does not exists");
            return;
        }

        configureLogback(logbackConfigurationFile);
    }

    protected void configureLogback(String logbackConfigurationFile) {
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
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    protected String getDefaultLogbackConfigurationFile() {
        String domibusConfigLocation = System.getProperty(DOMIBUS_CONFIG_LOCATION);
        if(StringUtils.isEmpty(domibusConfigLocation)) {
            LOG.error("The system property [" + DOMIBUS_CONFIG_LOCATION + "] is not configured" );
            return null;
        }

        return getLogFileLocation(domibusConfigLocation, DEFAULT_LOGBACK_FILE_NAME);
    }

    protected String getLogFileLocation(String domibusConfigLocation, String logFileName) {
        return domibusConfigLocation + File.separator + logFileName;
    }
}
