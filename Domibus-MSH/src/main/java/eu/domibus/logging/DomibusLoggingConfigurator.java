package eu.domibus.logging;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by Cosmin Baciu on 12-Oct-16.
 */
@Component
public class DomibusLoggingConfigurator  {

    private static final String DOMIBUS_CONFIG_LOCATION = "domibus.config.location";
    private static final String DEFAULT_LOG4J_FILE_NAME = "log4j.properties";

    private static final Log LOG = LogFactory.getLog(DomibusLoggingConfigurator.class);

    public void configureLogging(String configuredLog4jFilename) {
        String domibusConfigLocation = System.getProperty(DOMIBUS_CONFIG_LOCATION);
        configureLogging(domibusConfigLocation, configuredLog4jFilename);
    }

    public void configureLogging(String domibusConfigLocation, String configuredLog4jFilename) {
        if (StringUtils.isEmpty(domibusConfigLocation)) {
            LOG.warn("Could not configure logging: " + DOMIBUS_CONFIG_LOCATION + " is not configured.");
            return;
        }

        String log4jFileName = DEFAULT_LOG4J_FILE_NAME;
        if (StringUtils.isNotEmpty(configuredLog4jFilename)) {
            log4jFileName = configuredLog4jFilename;
            LOG.info("Using configured custom log4j file name: [" + configuredLog4jFilename + "]");
        }

        String log4jFilePath = getLogFileLocation(domibusConfigLocation, log4jFileName);
        if (!new File(log4jFilePath).exists()) {
            LOG.warn("Could not configure logging: the file [" + log4jFilePath + "] does not exists");
            return;
        }

        LOG.info("Using the log4j properties file from [" + log4jFilePath + "]");
        PropertyConfigurator.configure(log4jFilePath);
    }

    protected String getLogFileLocation(String domibusConfigLocation, String logFileName) {
        return domibusConfigLocation + File.separator + logFileName;
    }
}
