package eu.domibus.api.logging;

/**
 * @author Cosmin Baciu
 */
public interface LoggingConfigurator {

    void configureLogging();

    void configureLogging(String configurationFile);

    /**
     * it will return first the logging config file defined in the system property {@code logback.configurationFile}
     * and if this is not set then the one from the default location
     *
     * @return path to logback.xml file
     */
    String getLoggingConfigurationFile();
}
