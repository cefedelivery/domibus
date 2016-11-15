package eu.domibus.logging;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by Cosmin Baciu on 12-Oct-16.
 * @since 3.3
 */
@Component
public class DomibusLoggingConfigurator {

    private static final String DOMIBUS_CONFIG_LOCATION = "domibus.config.location";
    private static final String DEFAULT_LOG4J_FILE_NAME = "log4j.properties";
    private static final String LOG4J_FILE_PATH_PARAM = "log4jFilePath";

    private static final Log LOG = LogFactory.getLog(DomibusLoggingConfigurator.class);

    public void configureLogging() {
        String log4jFilePath = getDefaultLog4jFilePath();
        String customLog4jFilePath = System.getProperty(LOG4J_FILE_PATH_PARAM);
        if (StringUtils.isNotEmpty(customLog4jFilePath)) {
            LOG.info("Found custom log4j file: [" + customLog4jFilePath + "]");
            log4jFilePath = customLog4jFilePath;
        }
        configureLogging(log4jFilePath);
    }

    public void configureLogging(String log4jFilePath) {
        if (StringUtils.isEmpty(log4jFilePath)) {
            LOG.warn("Could not configure logging: the provided log4j file path is empty");
            return;
        }

        LOG.info("Using the log4j properties file from [" + log4jFilePath + "]");

        if (!new File(log4jFilePath).exists()) {
            LOG.warn("Could not configure logging: the file [" + log4jFilePath + "] does not exists");
            return;
        }

        PropertyConfigurator.configure(log4jFilePath);
    }

    protected String getDefaultLog4jFilePath() {
        String domibusConfigLocation = System.getProperty(DOMIBUS_CONFIG_LOCATION);
        if(StringUtils.isEmpty(domibusConfigLocation)) {
            LOG.error("The system property [" + DOMIBUS_CONFIG_LOCATION + "] is not configured" );
            return null;
        }

        return getLogFileLocation(domibusConfigLocation, DEFAULT_LOG4J_FILE_NAME);
    }

    protected String getLogFileLocation(String domibusConfigLocation, String logFileName) {
        return domibusConfigLocation + File.separator + logFileName;
    }
}
