package eu.domibus.core.logging;

import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public interface LoggingService {

    /**
     * Set the logging level for the given package or class
     *
     *
     * @param name
     * @param strLevel
     * @return true if the operation succed, false otherwise
     */
    boolean setLoggingLevel(final String name, final String strLevel);

    /**
     * Signal the set of logging level for the given package or class
     *
     * @param name
     * @param level
     */
    void signalSetLoggingLevel(final String name, final String level);

    /**
     * Get the logging levels for the given packages which starts or contains with {@code name} parameter
     *
     * @param loggerName
     * @param showClasses
     * @return
     */
    List<LoggingEntry> getLoggingLevel(final String loggerName, final boolean showClasses);

    /**
     * Reset the logging configuration to default
     * @return
     */
    boolean resetLogging();

    /**
     * signal the logging reset on a cluster env - send a messaage to command topic
     * @return
     */
    void signalResetLogging();
}
