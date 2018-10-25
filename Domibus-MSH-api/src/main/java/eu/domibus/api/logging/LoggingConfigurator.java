package eu.domibus.api.logging;

/**
 * @author Cosmin Baciu
 */
public interface LoggingConfigurator {

    void configureLogging();

    void configureLogging(String configurationFile);

    /**
     * it will return first the config file defined in the system property {@code 'logback.configurationFile'}
     * and if not the default one
     * @return path to logback.xml file
     */
    String getLogbackConfigurationFile();
}
