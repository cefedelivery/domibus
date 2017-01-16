package eu.domibus.api.logging;

/**
 * @author Cosmin Baciu
 */
public interface LoggingConfigurator {

    void configureLogging();

    void configureLogging(String configurationFile);
}
