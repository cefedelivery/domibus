package eu.domibus.core.logging;

import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.LoggingLevelResponseRO;
import eu.domibus.web.rest.ro.LoggingLevelResultRO;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public interface LoggingService {

    /**
     * Set the logging level for the given package or class
     *
     * @param loggingLevelRO
     * @return
     */
    LoggingLevelResponseRO setLoggingLevel(LoggingLevelRO loggingLevelRO);

    /**
     * Signal the set of logging level for the given package or class
     *
     * @param loggingLevelRO
     * @return
     */
    boolean signalSetLoggingLevel(LoggingLevelRO loggingLevelRO);

    /**
     * Get the logging levels for the given packages which starts or contains with {@code name} parameter
     *
     * @param loggerName
     * @param showClasses
     * @param page
     * @param pageSize
     * @return
     */
    LoggingLevelResultRO getLoggingLevel(final String loggerName, final boolean showClasses, int page, int pageSize);

    /**
     * Reset the logging configuration to default
     * @return
     */
    boolean resetLogging();

    /**
     * signal the logging reset on a cluster env - send a messaage to command topic
     * @return
     */
    boolean signalResetLogging();
}
